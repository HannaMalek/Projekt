package finanse.model;

import java.time.LocalDate;

public record Tranzakcja(double kwota, String typ, String kategoria, LocalDate data, String opis) {

    @Override
    public String toString() {
        return data + " | " + typ + " | " + kategoria + " | " + kwota + " | " + opis;
    }
}