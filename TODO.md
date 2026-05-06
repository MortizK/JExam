# Aufgaben

## Vorbereitung

### Aufgabe 1 - Versionsverwaltung mit Git

* Installieren Sie Git und einen grafischen Git-Client, z. B. TortoiseGit.
* Erstellen Sie lokal ein Repository und fügen Sie mehrfach verschiedene Dateien in mehreren Unterordnern dem Repository hinzu (mit add und commit).
* Verändern und löschen Sie einzelne Dateien bzw. Ordner und übertragen Sie diese Änderungen jeweils auch an das Repository.
* Schauen Sie sich die entsprechenden Logs und den aktuellen Stand des Repositories an. Gehen Sie dabei in der Versionshistorie bis an den Anfang zurück.
* Versuchen Sie, einen Merge-Konflikt zu erzeugen und aufzulösen.
* Wenn Sie vorher fertig sind, lesen Sie das Kapitel über Branching in ProGit: https://git-scm.com/book/de/v2/Git-Branching-Branches-auf-einen-Blick

### Aufgabe 2 - Vorbereitung der Anforderungsanalyse

* Stellen Sie sich vor, Sie arbeiten als Software-Entwickler und sollen für eine Hochschule ein Tool zur Erstellung von Klausuren entwickeln, bei dem Klausuren aus einem festen Satz an Prüfungsaufgaben generiert werden können.
* Bereiten Sie sich auf das Analysegespräch vor, in dem Sie möglichst viele gute Fragen unterschiedlicher Kategorien aufschreiben.

(Abgabe: 01_Analysefragen.pdf)

### Aufgabe 3 - Befragung des Kunden

* Befragen Sie jetzt den Kunden nach der zu entwickelnden Software.
* Notieren Sie sich dabei die Antworten, Sie benötigen diese für die Erstellung der Spezifikation.
* Versuchen Sie so genau wie möglich zu ermitteln, welche Software sich der Kunde wünscht und haken Sie bei Unklarheiten und Widersprüchen nach.

### Aufgabe 4 - Erstellung des Begriffslexikons

* Erstellen Sie ein erstes Begriffslexikon auf der Basis Ihrer Analysenotizen von Aufgabe 3. Beginnen Sie mit den wichtigsten 5-10 Begriffen.
* Im finalen Begriffslexikon müssen alle nicht offensichtlichen Fachbegriffe aus der gesamten Analyse definiert sein.
* Achten Sie auf möglichst kompakte, präzise Beschreibungen und fügen Sie auch Querverweise ein

(Abgabe: 02_Spezifikation.pdf, Kapitel Begriffslexikon)

### Aufgabe 5 - Modellierung der Use Cases

* Modellieren Sie die Anwendungsfälle (Use Cases) der Software mithilfe von UML.
* Stellen Sie die Use Cases grafisch dar und nutzen Sie die Dokumentationsschablone der vorigen Folie.

(Abgabe: 02_Spezifikation.pdf, Kapitel Anwendungsfälle)

## Spezifikation

### Aufgabe 6 - Modellierung der Benutzeroberfläche

* Installieren/starten Sie ein Werkzeug, mit dem Sie grafische Benutzeroberflächen modellieren können. Empfehlung: https://pencil.evolus.vn/ oder Figma
* Modellieren Sie die einzelnen Oberflächen des Versandkostenrechners.

(Abgabe: 02_Spezifikation.pdf, Kapitel Benutzeroberfläche)

### Aufgabe 7 - Paketdiagramm und Klassendiagramme

* Modellieren Sie die Architektur Ihrer Software zunächst grob mit einem einzigen UML-Paketdiagramm. Schreiben Sie zu jedem Paket 2-3 beschreibende Sätze.
* Verfeinern Sie dann Ihren Entwurf, indem Sie auf dieser Basis mehrere Klassendiagramme erstellen, die möglichst die komplette Datenstruktur der Anwendung abbilden soll

(Abgabe: 03_Entwurf.pdf)

### Aufgabe 8 - Abschluss der Spezifikation

* Finalisieren Sie die Spezifikation.
* Denken Sie dabei auch an eine Einleitung, an Angaben zum Mengengerüst, an nicht-funktionale Anforderungen
* Pflicht sind gut verständliche GUI-Prototypen aller Ansichten und dokumentierte Use Cases aller Anwendungsfälle

(Abgabe: 02_Spezifikation.pdf)

## Implementation

### Aufgabe 9 - Start der Implementierung

Beginnen Sie mit der Implementierung der Anwendung anhand der von Ihnen erstellen Spezifikation und anhand des Entwurfs.

* Es muss eine Java-Standalone-Anwendung sein, Sie können die UI-Bibliothek jedoch frei wählen
* Im Moodle finden Sie zur optionalen Verwendung ein Programmkonstrukt auf der Basis von JavaFX
* Vergessen Sie nicht, Ihren Code von Anfang an gut zu kommentieren. Verwenden Sie hierbei auch JavaDoc und generieren Sie die HTML- Dokumentation in regelmäßigen Abständen
* Vergessen Sie nicht, in regelmäßigen Abständen zu committen

### Aufgabe 10 - Implementierung des Grundgerüsts

Implementieren Sie Ihre Anwendung so weit, dass das Startfenster optisch fertig aussieht, insbesondere alle Buttons, Eingabefelder und spezielle UI-Elemente sichtbar sind und diese auch sinnvoll angeordnet sind. Dabei sollten bereits exemplarische Daten sichtbar sein.

Die Funktionalität hinter den Eingabeelementen muss jedoch noch nicht funktionieren.

Ergänzen Sie auf gleiche Weise die Implementierung alle weiteren wesentlichen Ansichten und Fenster der Anwendung. Kleine modale Dialoge (z. B. Speichern-Dialog, Rückfrage-Dialoge) können Sie noch weglassen.

### Aufgabe 12 - Fertigstellung des Prüflings

* Erstellen Sie eine einzelne PDF-Datei mit dem aktuellen Stand der
Spezifikation, inklusive Use Cases, Begriffslexikon und UI-Prototypen. Diese Datei wird der Prüfling des folgenden Reviews.
* Hinweis: Sie werden bei der folgenden Aufgabe (Reviewdurchführung) mit dem Prüfling einer anderen Person arbeiten. Behandeln Sie das Dokument vertraulich und nutzen Sie keine Inhalte daraus für die Abgabe der Spezifikation Ihrer eigenen Abgabe.

### Aufgabe 13 - Vorbereitung des technischen Reviews

Bereiten Sie sich je nach der Ihnen zugewiesenen Rolle auf das Review vor.

* Moderator – Machen Sie sich mit den Review-Regeln vertraut. Sie bekommen zusätzlich eine individuelle kurze Einweisung.
* Gutachter – Lesen Sie den Prüfling in Einzelarbeit durch und notieren sich alle Mängel, Fehler, Unklarheiten mit genauer Positionsangabe, am besten direkt im Dokument. Kommentieren Sie auch alle Besonderheiten im Text.
* Notar (hier gleichzeitig Autor) – Bereiten Sie das Protokoll der Review-Sitzung vor, im Idealfall mit einem Reviewprotokoll-Tool wie z. B. https://revager-org.github.io

### Aufgabe 14 - Durchführung des technischen Reviews

Setzen Sie sich in den Review-Gruppen zusammen und starten Sie das Review. Achten Sie auf die Ihnen zugewiesene Rolle.

Nach Abschluss des Reviews lädt der Notar das Review-Protokoll in Moodle hoch.

### Aufgabe 15 - JUnit-Testfälle

Erstellen Sie möglichst gute JUnit-Testfälle für den PackageCalculator. Orientieren Sie sich an den Anforderungen, die in Aufgabe 3 genannt sind und überlegen Sie sich, welche Äquivalenzklassen Sie daraus bilden. Jede Äquivalenzklasse muss durch mindestens einen Testfall abgedeckt sein. Denken Sie daran, auch für die Testfälle Code-Kommentare zu erstellen. Die GUI müssen Sie nicht testen.

### Aufgabe 16 - Glass-Box-Test

* Installieren Sie ein Glassbox-Testing-Tool, Sie finden diese auch unter der Bezeichnung „Code Coverage Measurement Tool“. Messen Sie die Anweisungsüberdeckung Ihrer bestehenden JUnit-Testfälle.
* Fügen Sie Testfälle hinzu, so dass Sie möglichst 100% Anweisungsüberdeckung und 100% Zweigüberdeckung haben.

Beispiele einzelner Tools:

* Eclipse: https://www.eclemma.org/
* IntelliJ IDEA: https://www.jetbrains.com/help/idea/code-coverage.html
* Andere IDEs: https://en.wikipedia.org/wiki/Java_code_coverage_tools

### Aufgabe 17 - Zufallsbasierter Test

* Erstellen Sie einen JUnit-Testfall, der 1000 Pakete verschiedener Größen zufallsbasiert in einer Schleife erstellt. Achten Sie auf sinnvolle Wertebereiche.
* Für jedes Paket eines Testfalls muss das Porto errechnet (Ist-Resultat) und überprüft werden (d. h. mit dem Sollresultat verglichen). Schreiben Sie hierzu eine separate Funktion, die das Soll-Resultat auf der Basis der Daten eines Pakets generiert.
* Fügen Sie weitere Bedingungen hinzu, die überprüft werden, z. B. dass das Porto niemals kleiner 0 oder größer 100 sein darf

Hinweis: Auch dieser Testfall ist Teil des Gesamtprojekts, das Sie am Ende des Semesters abgeben.

### Aufgabe 18 - Metriken

* Erfassen Sie die wesentlichen Metriken Ihrer Implementierung (Lines of Code, Anzahl Pakete, Anzahl Klassen).
* Prüfen Sie, wie Sie weitere Metriken Ihres Projekts mit Ihrer IDE erfassen können, z. B. Kopplung (Coupling), Zusammenhalt (Cohesion), durchschnittliche Methodengröße, Code-Kommentar-Anteil
* Nutzen Sie ggf. weitere Plugins für Ihre IDE (Suchwort-Beispiele: „Metrics Plugin“, „Measurement Plugin“, „Static Code Analyzer“), z. B. CodeMR für Eclipse: https://marketplace.eclipse.org/content/codemr-static-code-analyser

### Aufgabe 19 - Messung des Gesamtfortschritts

* Bewerten Sie für jeden der Use Cases in Ihrer Spezifikation, wie weit Ihre Implementierung fortgeschritten ist. Nutzen Sie jeweils eine Skala von 0-3 (0 = Use Case noch nicht umgesetzt, 3 = Use Case vollständig umgesetzt).
* Addieren Sie alle Bewertungen und teilen diese durch die Anzahl der Use Cases

### Aufgabe 20 - Refactoring

* Schauen Sie sich alle Refactoring-Pattern an, die unter folgender Webseite genannt und erläutert sind: https://refactoring.com/catalog/index.html
* Wählen Sie 3 beliebige der dort genannten Pattern aus und beurteilen Sie für jedes Pattern einzeln, an welcher Stelle im vorhandenen Code die Anwendung das jeweiligen Pattern Sinn machen könnte.
* Dokumentieren Sie Ihre Überlegungen und Entscheidungen in 2-3 Sätzen pro Pattern (ggf. mit Positionsangaben im Code) in einem Textdokument.
* Dateiname der Abgabe: "Refactoring_Report.pdf"

(Abgabe: "Refactoring_Report.pdf")

### Aufgabe 21 - Bug Reporting

Schreiben Sie für jede der vier Arten von Wartung einen sinnvollen Software Problem Reports (Tickets/Issues) nach dem Schema der Folie „Bug Reports“ für den aktuellen Stand Ihrer Software.

Nutzen Sie hierfür ein Textverarbeitungsprogramm Ihrer Wahl, exportieren das Dokument als PDF-Datei "Tickets.pdf", legen Sie es in Ihrem Projektordner ab. Geben Sie die Datei am Ende des Semester zusammen mit Ihrem gesamten Projekt mit ab.

(Abgabe: "Tickets.pdf")

### Aufgabe 22 - Ergänzung der UI

* Bauen Sie zu folgenden drei UI-Prinzipien nach der vorigen Folie (ELEGANCE-Prinzipien) möglichst kreativ je eine Ergänzung in Ihr Programm ein:
  * Empathy
  * Guidance
  * Novelty
* Fügen Sie im Quellcode an entsprechender Stelle jeweils einen Kommentar hinzu, z. B.: // UI/UX-Rule "Empathy"
* Prüfen und verbessern Sie Ihre Benutzeroberfläche anhand der in der Vorlesung genannten UX/UI-Prinzipien.

# Zusammenfassung

Dieses Dokument ist ein vollständiger Arbeitsplan für das Projekt JExam. Es führt die Arbeit von der Vorbereitung über Analyse, Spezifikation und Entwurf bis zu Implementierung, Tests, Metriken, Refactoring, Review und UI-Verbesserungen. Für die Abgabe sind vor allem die Dokumente 01_Analysefragen.pdf, 02_Spezifikation.pdf, 03_Entwurf.pdf, Refactoring_Report.pdf und Tickets.pdf relevant.

## To Dos aus den Aufgaben oben

- [x] Analysefragen für das Kundengespräch finalisieren und als 01_Analysefragen.pdf abgeben
- [x] Kundengespräch führen, Antworten sauber notieren und die Analysenotizen sichern
- [x] Begriffslexikon mit allen wichtigen Fachbegriffen vervollständigen
- [x] Use Cases modellieren, dokumentieren und in die Spezifikation übernehmen
- [x] UI-Prototypen aller relevanten Ansichten erstellen
- [x] Paketdiagramm und Klassendiagramme für den Entwurf ausarbeiten
- [x] Spezifikation mit Einleitung, Mengengerüst und nicht-funktionalen Anforderungen finalisieren
- [x] Implementierung des Grundgerüsts und aller wesentlichen Ansichten abschließen
- [x] Prüfling als einzelne PDF-Datei erzeugen
- [x] Vorbereitung und Durchführung des technischen Reviews erledigen
- [X] JUnit-Testfälle für den PackageCalculator schreiben
- [x] Glass-Box-Tests ergänzen, bis die Coverage möglichst vollständig ist
- [x] Zufallsbasierten JUnit-Test für 1000 Pakete implementieren
- [x] Wesentliche Metriken des Projekts erfassen
- [x] Gesamtfortschritt der Use Cases bewerten
- [x] Refactoring-Report mit 3 geeigneten Patterns schreiben
- [x] Vier Software-Problem-Reports als Tickets.pdf erstellen
- [x] UI um Empathy, Guidance und Novelty ergänzen und im Code kommentieren

## Offene To Dos

- [ ] Alle Texte sollen Deutsch, Englisch Support bekommen.
- [x] PDF Gen von Aufgaben überarbeiten
- [ ] PDF Gen von Deckblatt überarbeiten
- [ ] Fix Difficulty Distribution für PDF Gen
  - [ ] Silent Error oder Vereinfachung der Aktuellen Fehlermeldung
  - [ ] Difficulty Distribution soll für die Ganze Klausur gelten, nicht nur pro Chapter
- [ ] Remove Custom Keyboard movement
- [ ] Metriken
  - [ ] Nicht nur Volumen Metriken
  - [ ] Noch Kohäsion messen und hinzufügen
- [ ] PDF Gen Secret Zitate für Mock Exams und Exams einfügen
- [ ] Fix Dark-Theme

## To Dos für die Live Demo am 20.05.2026

- [ ] Auszug der Testfälle
- [ ] Quellcode zeigen
- [ ] Use Cases zeigen
- [ ] Javadoc-Seiten in HTML
- [ ] Besonderheiten zeigen
- [ ] Live Demo Vorbereiten
  - [ ] Ablauf aufschreiben
  - [ ] Ablauf durchführen
  - [ ] Ablauf verbessern
  - [ ] Ablauf Prüfen

## To Dos für V.2

- [ ] PDF Gen soll einstellungen haben
  - [ ] Mit Ohne Deckblatt
  - [ ] Fußzeile Auswahl (Seitenzahl mit/ ohne, Start mit ...)
- [ ] Einstellungen für Standard Out von PDFs
- [ ] Dark/ Light Mode in die Einstellungen
- [ ] Show Keyboard shortcuts where applicable (Strg + S für Save/ Speichern)