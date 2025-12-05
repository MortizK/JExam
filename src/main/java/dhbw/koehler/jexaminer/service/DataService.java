package dhbw.koehler.jexaminer.service;

import dhbw.koehler.jexaminer.model.Exam;

public class DataService {
    public Exam exam;

    public DataService(String name) {
        this.exam = new Exam((name == null) ? "Exam" : name);
    }
}
