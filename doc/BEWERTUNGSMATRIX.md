# Bewertungsmatrix - JExam
## Software Engineering 1 - Abgabe nach zwei Semestern

**Bewertungsbasis:** aktueller Workspace-Stand, `TODO.md`, `README.md`, `doc/Metriken.md`, vorhandene PDFs, Java-Quellstruktur und Test-Artefakte.

---

## Bewertungsskala

| Punkte | Bedeutung |
|---|---|
| 0 | nicht vorhanden / nicht erkennbar |
| 1 | ansatzweise vorhanden |
| 2 | weitgehend vorhanden |
| 3 | vollständig bzw. überzeugend umgesetzt |
| n.v. | bewusst nicht bewertet |

Hinweis: Bei UI- und Usability-Bewertungen bleiben die Bewertungsfelder absichtlich leer.

---

## Phase 1 - Vorbereitung

| Aufgabe | Status | Punkte | Kurzbewertung | Evidenz / Einordnung |
|---|---|---:|---|---|
| 1 - Versionsverwaltung mit Git | erfüllt | 3 | sauber umgesetzt | Die Git-Historie ist vorhanden, es gibt mehrere Commits sowie Branches wie `main`, `Refactoring` und `docs/developer-guide`. |
| 2 - Vorbereitung der Anforderungsanalyse | erfüllt | 3 | vollständig dokumentiert | `01_Analysefragen.pdf` ist als Abgabe vorhanden; die Aufgabe wurde im TODO als erledigt markiert. |
| 3 - Befragung des Kunden | erfüllt | 3 | fachlich sinnvoll vorbereitet | Die Anforderungsbasis ist im Spezifikationspaket erkennbar; die Aufgabe ist im TODO als erledigt markiert. |
| 4 - Begriffslexikon | erfüllt | 3 | konsistent und verwendbar | Das Glossar ist laut TODO abgeschlossen und wird durch die Spezifikationsartefakte gestützt. |
| 5 - Use Cases modellieren | erfüllt | 3 | sauber in die Spezifikation integriert | Die Use-Cases sind Teil der Spezifikation; die TODO-Liste führt die Aufgabe als erledigt. |

**Zwischensumme Phase 1: 15 / 15 Punkte**

---

## Phase 2 - Spezifikation

| Aufgabe | Status | Punkte | Kurzbewertung | Evidenz / Einordnung |
|---|---|---:|---|---|
| 6 - Benutzeroberfläche modellieren | weitgehend erfüllt | 2 | gute Grundlage, aber nicht vollständig nachweisbar | Im aktuellen Workspace sind vor allem die umgesetzten UI-Komponenten sichtbar; explizite Prototypen-Dateien aus einem Zeichenwerkzeug sind nicht auffindbar. |
| 7 - Paketdiagramm und Klassendiagramme | weitgehend erfüllt | 2 | Architektur klar, Diagramm-Artefakt nicht direkt prüfbar | Die Paketstruktur ist im Code sehr klar: `com.jexam.app`, `io`, `generation`, `model`, `validation` sowie UI-Unterpakete. |
| 8 - Abschluss der Spezifikation | erfüllt | 3 | abgabereif | `02_Spezifikation.pdf` ist vorhanden und wurde laut TODO finalisiert. |

**Zwischensumme Phase 2: 7 / 9 Punkte**

---

## Phase 3 - Implementierung

| Aufgabe | Status | Punkte | Kurzbewertung | Evidenz / Einordnung |
|---|---|---:|---|---|
| 9 - Start der Implementierung | erfüllt | 3 | tragfähiges Grundgerüst | Unter `src/main/java` liegt eine vollständige Java-Standalone-Anwendung mit den Kernpaketen für App, IO, Generation, Model und Validation vor. |
| 10 - Grundgerüst der Anwendung | erfüllt | 3 | umfangreiche UI umgesetzt | Es gibt mehrere Komponenten für XML- und PDF-Ansichten, Auswahl, Layout und Stil. |
| 11 - nicht im TODO enthalten | n.v. | n.v. | nicht Teil dieser Matrix | Im vorliegenden TODO wird diese Nummer übersprungen. |
| 12 - Prüfling als einzelne PDF-Datei | erfüllt | 3 | vorhanden | Die PDF-Abgabe ist vorhanden; die Spezifikation wurde als einzelne Datei exportiert. |

**Zwischensumme Phase 3: 9 / 9 Punkte**

---

## Phase 4 - Testing

| Aufgabe | Status | Punkte | Kurzbewertung | Evidenz / Einordnung |
|---|---|---:|---|---|
| 13 - Vorbereitung des technischen Reviews | weitgehend erfüllt | 2 | als erledigt markiert, Artefakt nicht direkt sichtbar | Die TODO-Liste führt die Aufgabe als erledigt. Ein separates Review-Protokoll lag im aktuellen Workspace jedoch nicht als direkt prüfbares Artefakt vor. |
| 14 - Durchführung des technischen Reviews | weitgehend erfüllt | 2 | plausibel abgeschlossen | Auch diese Aufgabe ist im TODO abgehakt. |
| 15 - JUnit-Testfälle | erfüllt | 3 | breiter Testunterbau | Es existieren 23 Testklassen in `src/test/java`, darunter Tests für Modell, IO, Generation, App-Layer und Workflow. |
| 16 - Glass-Box-Test | erfüllt | 3 | Tooling vorhanden | `pom.xml` bindet JaCoCo ein, und im Workspace liegen Metrik- und Report-Artefakte vor. |
| 17 - Zufallsbasierter Test | erfüllt | 3 | direkt nachweisbar | `src/test/java/com/jexam/workflow/RandomDatasetWorkflowTest.java` ist vorhanden. |
| 18 - Metriken erfassen | erfüllt | 3 | sehr gut dokumentiert | `doc/Metriken.md` dokumentiert reproduzierbare Projektmetriken wie Dateianzahl, Paketanzahl, Kommentaranteil, durchschnittliche Methodengröße und Kopplungswerte. |
| 19 - Gesamtfortschritt der Use Cases | weitgehend erfüllt | 2 | als Projektziel erkennbar, aber nicht als separate Tabelle belegt | Die TODO-Liste markiert die Aufgabe als erledigt. Ein explizites Scoring-Dokument pro Use Case liegt im aktuellen Workspace nicht direkt vor. |

**Zwischensumme Phase 4: 18 / 21 Punkte**

---

## Phase 5 - Wartung, Refactoring und Qualitätssicherung

| Aufgabe | Status | Punkte | Kurzbewertung | Evidenz / Einordnung |
|---|---|---:|---|---|
| 20 - Refactoring | erfüllt | 3 | klar dokumentiert | `Refactoring_Report.pdf` ist vorhanden; zusätzlich dokumentiert `doc/Metriken.md` konkrete Qualitätskennzahlen. |
| 21 - Bug Reporting | erfüllt | 3 | sauber abgegeben | `Tickets.pdf` ist vorhanden und als Abgabe markiert. |
| 22 - Ergänzung der UI | n.v. | n.v. | UI/Usability-Bewertung bewusst leer | Die inhaltliche Aufgabe ist im TODO als erledigt markiert, aber die eigentliche UI-/Usability-Bewertung bleibt hier absichtlich frei. |

**Zwischensumme Phase 5: 6 / 6 Punkte**

---

## Projektmetriken aus der vorhandenen Dokumentation

| Kennzahl | Wert | Einordnung |
|---|---:|---|
| Java-Dateien gesamt | 79 | ausreichend groß für ein Semesterprojekt mit klarer Schichtung |
| Java-Dateien in `src/main/java` | 56 | zeigt ein substantielles Implementierungsvolumen |
| Java-Dateien in `src/test/java` | 23 | solide Testbasis |
| Pakete gesamt | 14 | gute funktionale Zerlegung |
| Klassen / Interfaces / Enums / Records | 24 | überschaubare, aber strukturierte Domäne |
| Methoden / Konstruktoren | 658 | deutlicher Funktionsumfang |
| Kommentaranteil | 24,68 % | für ein gut dokumentiertes Studienprojekt ordentlich |
| Durchschnittliche Methodengröße | 12,52 Code-Zeilen | im akzeptablen Bereich, aber mit weiterem Optimierungspotenzial |
| Efferente Kopplung | 35 | für die Größe des Projekts plausibel, aber nicht minimal |

---

## UI- und Usability-Bewertung

Die folgenden Spalten bleiben absichtlich leer, wie gewünscht.

| Aspekt | Status | Bewertung | Kommentar |
|---|---|---|---|
| Usability - Ergonomie und Navigation |  |  |  |
| Usability - Fehlermeldungen und Nutzerführung |  |  |  |
| Visual Design - Layout und Konsistenz |  |  |  |
| Visual Design - Farbschema und Lesbarkeit |  |  |  |
| Accessibility - Tastaturnavigation |  |  |  |
| Accessibility - Screenreader-Unterstützung |  |  |  |
| Empathy |  |  |  |
| Guidance |  |  |  |
| Novelty |  |  |  |

---

## Gesamtbild

| Bereich | Erreicht | Maximal | Quote |
|---|---:|---:|---:|
| Phase 1 - Vorbereitung | 15 | 15 | 100 % |
| Phase 2 - Spezifikation | 7 | 9 | 77,8 % |
| Phase 3 - Implementierung | 9 | 9 | 100 % |
| Phase 4 - Testing | 18 | 21 | 85,7 % |
| Phase 5 - Wartung und Qualität | 6 | 6 | 100 % |
| **Gesamt ohne UI/UX** | **55** | **60** | **91,7 %** |

---

## Qualitative Gesamteinschätzung

Das Projekt wirkt insgesamt reif und gut strukturiert. Besonders stark sind die Implementierung, die Testbasis und die Dokumentationsspur: Es gibt eine klare Paketstruktur, eine beachtliche Zahl an Tests, ein randomisiertes Test-Szenario, eine Metrikendokumentation und mehrere fertig abgegebene PDF-Artefakte.

Die Spezifikation und die Architektur sind insgesamt überzeugend, auch wenn die im Workspace sichtbaren Nachweise für grafische UI-Prototypen und formale Architekturdiagramme nicht ganz so stark sind wie die Nachweise für Code, Tests und Metriken. Das ist der Hauptgrund, warum ich bei den Spezifikationsaufgaben konservativer bewerte.

Die größten offenen Punkte liegen im UI-nahen Feinschliff und in einigen Folgearbeiten aus dem TODO-Bereich, etwa Dark Theme, Keyboard-Movement und zusätzliche PDF-Optionen. Diese Punkte mindern den Gesamteindruck nicht stark, zeigen aber, dass das Projekt zwar abgabereif, aber noch nicht in jeder Hinsicht final poliert ist.

## Kurzfazit

Für ein SE1-Projekt mit zwei Semestern Bearbeitungszeit ist das eine sehr gute bis ausgezeichnete Abgabe. Ich würde das Gesamtbild klar im Bereich "gut bis sehr gut" einordnen, mit besonders starken Bereichen bei Implementierung, Testing, Refactoring und Metriken.