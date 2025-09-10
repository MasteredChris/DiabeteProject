package controller;

import model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataController {

    // Helper generico per salvare dati dei pazienti senza sovrascrivere altri pazienti
    private void salvaConMerge(
            String filePath,
            List<Paziente> pazientiModificati,
            Function<String, String> extractPazienteId,
            Function<Paziente, List<String>> generaRighe,
            String intestazione
    ) {
        List<String> righe = new ArrayList<>();

        // 1. Leggi il file esistente
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header != null) righe.add(header);

            String line;
            while ((line = br.readLine()) != null) {
                String pazienteId = extractPazienteId.apply(line);
                boolean daSostituire = pazientiModificati.stream()
                        .anyMatch(p -> String.valueOf(p.getId()).equals(pazienteId));
                if (!daSostituire) righe.add(line);
            }
        } catch (IOException e) {
            // Se il file non esiste, creeremo la nuova intestazione
            righe.add(intestazione);
        }

        // 2. Aggiungi le righe dei pazienti modificati
        for (Paziente p : pazientiModificati) {
            righe.addAll(generaRighe.apply(p));
        }

        // 3. Scrivi il file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- UTENTI ----------
    public List<Utente> caricaUtenti(String utentiFile) {
        List<Utente> utenti = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(utentiFile))) {
            String riga;
            boolean primaRiga = true;
            while ((riga = br.readLine()) != null) {
                if (primaRiga) { primaRiga = false; continue; }
                String[] campi = riga.split(",");
                if (campi.length < 6) continue;

                int id = Integer.parseInt(campi[0].trim());
                String type = campi[1].trim();
                String nome = campi[2].trim();
                String cognome = campi[3].trim();
                String email = campi[4].trim();
                String password = campi[5].trim();

                if (type.equalsIgnoreCase("Paziente")) {
                    int medicoId = Integer.parseInt(campi[6].trim());
                    utenti.add(new Paziente(id, nome, cognome, email, password, medicoId));
                } else if (type.equalsIgnoreCase("Diabetologo")) {
                    utenti.add(new Diabetologo(id, nome, cognome, email, password));
                }
            }
            associaPazientiAiMedici(utenti);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return utenti;
    }

    private void associaPazientiAiMedici(List<Utente> utenti) {
        Map<Integer, Diabetologo> mediciMap = utenti.stream()
                .filter(u -> u instanceof Diabetologo)
                .map(u -> (Diabetologo) u)
                .collect(Collectors.toMap(Diabetologo::getId, d -> d));

        utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .forEach(p -> {
                    Diabetologo medico = mediciMap.get(p.getMedicoId());
                    if (medico != null) {
                        p.setMedico(medico);
                        medico.addPaziente(p);
                    }
                });
    }

    // ---------- RILEVAZIONI ----------
    public void caricaRilevazioni(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // salta intestazione
            while ((line = br.readLine()) != null) {
                String[] campi = line.split(",");
                if (campi.length < 4) continue;
                int id = Integer.parseInt(campi[0].trim());
                Paziente p = pazientiMap.get(id);
                if (p != null) {
                    p.aggiungiRilevazione(new Rilevazione(
                            LocalDate.parse(campi[1].trim()),
                            campi[2].trim(),
                            Integer.parseInt(campi[3].trim())
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvaRilevazioni(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(",")[0],
                p -> p.getRilevazioni().stream()
                        .map(r -> p.getId() + "," + r.getData() + "," + r.getTipoPasto() + "," + r.getValore())
                        .toList(),
                "pazienteId,data,tipoPasto,valore");
    }

    // ---------- TERAPIE ----------
    public void caricaTerapie(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // intestazione
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = line.split(",");
                if (c.length < 9) continue;
                Paziente p = pazientiMap.get(Integer.parseInt(c[0]));
                if (p != null) {
                    p.aggiungiTerapia(new Terapia(
                            c[1], Integer.parseInt(c[2]), Double.parseDouble(c[3]), c[4],
                            LocalDate.parse(c[5]), LocalDate.parse(c[6]), Terapia.Stato.valueOf(c[7]),
                            Integer.parseInt(c[8])
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvaTerapie(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(",")[0],
                p -> p.getTerapie().stream()
                        .map(t -> p.getId() + "," + t.getFarmaco() + "," + t.getAssunzioniGiornaliere() + "," +
                                t.getQuantitaPerAssunzione() + "," + t.getIndicazioni() + "," +
                                t.getDataInizio() + "," + t.getDataFine() + "," +
                                t.getStato() + "," + t.getMedicoId())
                        .toList(),
                "pazienteId,farmaco,assunzioniGiornaliere,quantitaPerAssunzione,indicazioni,dataInizio,dataFine,stato,medicoId");
    }

    // ---------- ASSUNZIONI ----------
    public void caricaAssunzioni(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // intestazione
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = line.split(",");
                if (c.length < 5) continue;
                Paziente p = pazientiMap.get(Integer.parseInt(c[0]));
                if (p != null) {
                    p.aggiungiAssunzione(new Assunzione(
                            LocalDate.parse(c[1]), LocalTime.parse(c[2]), c[3], Double.parseDouble(c[4])
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvaAssunzioni(String filePath, List<Paziente> pazientiModificati) {
        salvaConMerge(
                filePath,
                pazientiModificati,
                riga -> riga.split(",")[0],
                p -> {
                    List<String> righe = new ArrayList<>();
                    for (Assunzione a : p.getAssunzioni())
                        righe.add(p.getId() + "," + a.getData() + "," + a.getOra() + "," + a.getFarmaco() + "," + a.getQuantita());
                    return righe;
                },
                "pazienteId,data,ora,farmaco,quantita"
        );
    }

        // ---------- SCHEDE CLINICHE ----------
    public void caricaSchedeCliniche(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // intestazione
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = line.split(",", -1);
                if (c.length < 4) continue;
                Paziente p = pazientiMap.get(Integer.parseInt(c[0]));
                if (p != null) {
                    p.setSchedaClinica(new SchedaClinica(c[1], c[2], c[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvaSchedeCliniche(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(",")[0],
                p -> {
                    SchedaClinica s = p.getSchedaClinica();
                    if (s == null) s = new SchedaClinica();
                    return List.of(p.getId() + "," +
                            (s.getFattoriRischio() == null ? "" : s.getFattoriRischio()) + "," +
                            (s.getPregressePatologie() == null ? "" : s.getPregressePatologie()) + "," +
                            (s.getComorbidita() == null ? "" : s.getComorbidita()));
                },
                "pazienteId,fattoriRischio,pregressePatologie,comorbidita");
    }

    // ---------- EVENTI CLINICI ----------

    public void caricaEventiClinici(String filePath, List<Utente> utenti) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String riga;
            boolean primaRiga = true;

            while ((riga = br.readLine()) != null) {
                if (primaRiga) {
                    primaRiga = false;
                    continue; // salta l'intestazione
                }

                String[] campi = riga.split(",", -1); // -1 per preservare campi vuoti
                if (campi.length < 6) continue;

                int pazienteId = Integer.parseInt(campi[0].trim());
                String tipo = campi[1].trim();
                String descrizione = campi[2].trim();
                LocalDate data = LocalDate.parse(campi[3].trim());
                LocalTime ora = LocalTime.parse(campi[4].trim());
                String note = campi[5].trim();

                EventoClinico evento = new EventoClinico(tipo, descrizione, data, ora, note);

                for (Utente u : utenti) {
                    if (u instanceof Paziente p && p.getId() == pazienteId) {
                        p.aggiungiEventoClinico(evento);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void salvaEventiClinici(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(",")[0],
                p -> p.getEventiClinici().stream()
                        .map(e -> p.getId() + "," +
                                e.getTipo() + "," +
                                e.getDescrizione() + "," +
                                e.getData() + "," +
                                (e.getOra() != null ? e.getOra() : "") + "," +
                                (e.getNote() != null ? e.getNote() : ""))
                        .toList(),
                "pazienteId,tipo,descrizione,data,ora,note");
    }


    // ---------- TERAPIE CONCOMITANTI ----------
    public void caricaTerapieConcomitanti(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // salta intestazione
            String line;
            while ((line = br.readLine()) != null) {
                String[] campi = line.split(",", -1); // -1 per preservare campi vuoti
                if (campi.length < 3) continue;
                int pazienteId = Integer.parseInt(campi[0].trim());
                String tipo = campi[1].trim();
                String descrizione = campi[2].trim();

                Paziente p = pazientiMap.get(pazienteId);
                if (p != null) {
                    p.aggiungiTerapiaConcomitante(new TerapiaConcomitante(tipo, descrizione));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvaTerapieConcomitanti(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(",")[0],
                p -> p.getTerapieConcomitanti().stream()
                        .map(t -> p.getId() + "," + t.getTipoTerapia() + "," +
                                (t.getDescrizione() != null ? t.getDescrizione() : ""))
                        .toList(),
                "pazienteId,tipoTerapia,descrizione");
    }



}
