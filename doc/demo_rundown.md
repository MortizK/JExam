# Demo Rundown — Live Demo 20.05.2026

Kurz: 10 Minuten Live-Demo, Fokus auf Kernfunktionalität, Tests und Dokumentation.

## Zeitplan (10 Minuten)

- 0:00–0:45 — Begrüßung & Ziel der Demo (Technologie-Stack: Java, Maven, JUnit, Jacoco, JavaDoc, PDF-Export)
- 0:45–2:00 — Kurzer Code-Überblick: Projektstruktur und zentrale Module
- 2:00–3:30 — Tests ausführen: `mvn test` und Surefire-Report zeigen
- 3:30–4:30 — Coverage: Jacoco-Bericht öffnen (`target/site/jacoco/index.html`)
- 4:30–5:30 — JavaDoc zeigen (`target/site/apidocs/index.html`)
- 5:30–7:30 — Live-Feature: App starten, Beispielklausur generieren und PDF exportieren
- 7:30–8:30 — Optional: Randomized test (1000 Pakete) kurz erläutern und Ergebnis zeigen
- 8:30–9:15 — Artefakte und Dokumentation (README, TODO, generierte Reports)
- 9:15–10:00 — Abschluss: Zusammenfassung, offene Punkte, Fragen

## Wichtige Dateien (kurz zeigen)

- `src/main/java/com/jexam/app` — Hauptanwendung und UI-Startpunkt
- `src/main/java/com/jexam/generation` — PDF-/Exam-Generation-Services
- `src/main/java/com/jexam/model` — Domänenmodelle (Exam, Task, Chapter)
- `src/test/java` — JUnit-Tests, insbesondere `PackageCalculator`-Tests und der 1000‑Pakete Test
- `README.md`, `TODO.md` — Projektübersicht und Live-Demo-Checkliste

## Wichtige Befehle (für die Demo)

- Tests: `mvn test`
- Einzeltest (falls benötigt): `mvn -Dtest=PackageCalculatorTest test`
- App starten: `mvn exec:java -Dexec.mainClass=com.jexam.app.Main`

## Tests & Coverage

- Tests laufen lassen: `mvn test` — danach `target/surefire-reports` öffnen
- Coverage: `target/site/jacoco/index.html` im Browser öffnen und PackageCalculator abdecken
- Randomized test: in `src/test/java` nach dem Testfall `RandomDatasetWorkflowTest` bzw. dem 1000‑Pakete Test suchen und kurz Zweck erklären

## Live-Demo-Checkliste

- Vor Demo: IDE öffnen, Browser mit Jacoco/JavaDoc/PDF bereitstellen
- Terminal-Tab: Tests (mvn test) bereit
- Start-App-Tab: `mvn exec:java` vorbereitet
- Beispiel-Datensatz laden oder kurz erzeugen
- PDF-Export durchführen und Beispiel-PDF bereithalten

## Gesprächspunkte / Talking Points

- Design: Services, Interfaces, Trennung von UI und Logik
- Teststrategie: Unit-Tests, randomized tests, Coverage-Ziele
- Dokumentation: JavaDoc-Qualität und generierte HTML-Seiten
- Offene ToDos für V.2 (kurz nennen): Dark-Theme, PDF-Optionen, UI Shortcuts

