package finanse.ui;

import finanse.model.Tranzakcja;
import finanse.serwis.SerwisTranzakcji;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class KonsolaUI {

    private final SerwisTranzakcji serwis;
    private final Scanner scanner;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public KonsolaUI() {
        serwis = new SerwisTranzakcji();
        scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== PROGRAM DO ZARZADZANIA FINANSAMI ===");
            System.out.println("1. Dodaj tranzakcje");
            System.out.println("2. Wyswietl wszystkie tranzakcje");
            System.out.println("3. Oblicz bilans");
            System.out.println("4. Filtruj po typie");
            System.out.println("5. Filtruj po kategorii");
            System.out.println("6. Zapisz tranzakcje do pliku (tranzakcje.csv)");
            System.out.println("7. Wczytaj tranzakcje z pliku (tranzakcje.csv)");
            System.out.println("8. Raport miesieczny w konsoli");
            System.out.println("9. Eksport raportu miesiecznego do CSV");
            System.out.println("0. Wyjscie");
            System.out.print("Wybierz opcje: ");

            String opcja = scanner.nextLine();

            switch (opcja) {
                case "1" -> dodajTranzakcje();
                case "2" -> wyswietlWszystkie();
                case "3" -> pokazBilans();
                case "4" -> filtrujPoTypie();
                case "5" -> filtrujPoKategorii();
                case "6" -> serwis.zapiszDoPliku();
                case "7" -> serwis.wczytajZPliku();
                case "8" -> pokazRaportMiesieczny();
                case "9" -> eksportRaportu();
                case "0" -> running = false;
                default -> System.out.println("Niepoprawna opcja");
            }
        }
    }

    private void dodajTranzakcje() {
        double kwota;
        while (true) {
            System.out.print("Kwota: ");
            String kwotaStr = scanner.nextLine();
            try {
                kwota = Double.parseDouble(kwotaStr);
                if (kwota <= 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("BLAD: Kwota musi byc liczba wieksza od 0");
            }
        }

        String typ;
        while (true) {
            System.out.print("Typ (PRZYCHOD/WYDATEK): ");
            typ = scanner.nextLine().toUpperCase();
            if (typ.equals("PRZYCHOD") || typ.equals("WYDATEK")) break;
            System.out.println("BLAD: Typ musi byc PRZYCHOD lub WYDATEK");
        }

        System.out.print("Kategoria: ");
        String kategoria = scanner.nextLine();

        LocalDate data;
        while (true) {
            System.out.print("Data (yyyy-MM-dd): ");
            String dataStr = scanner.nextLine();
            try {
                data = LocalDate.parse(dataStr, dtf);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("BLAD: Niepoprawny format daty");
            }
        }

        System.out.print("Opis: ");
        String opis = scanner.nextLine();

        Tranzakcja t = new Tranzakcja(kwota, typ, kategoria, data, opis);
        serwis.dodajTranzakcje(t);
        System.out.println("Dodano tranzakcje!");
    }

    private void wyswietlWszystkie() {
        List<Tranzakcja> lista = serwis.wszystkieTranzakcje();
        if (lista.isEmpty()) System.out.println("Brak tranzakcji");
        else {
            System.out.println("=== WSZYSTKIE TRANZAKCJE ===");
            lista.forEach(System.out::println);
        }
    }

    private void pokazBilans() {
        double bilans = serwis.obliczBilans();
        System.out.println("=== BILANS ===");
        System.out.println("Bilans: " + bilans);
    }

    private void filtrujPoTypie() {
        System.out.print("Typ (PRZYCHOD/WYDATEK): ");
        String typ = scanner.nextLine().toUpperCase();
        List<Tranzakcja> lista = serwis.filtrujPoTypie(typ);
        if (lista.isEmpty()) System.out.println("Brak tranzakcji dla tego typu");
        else lista.forEach(System.out::println);
    }

    private void filtrujPoKategorii() {
        System.out.print("Kategoria: ");
        String kategoria = scanner.nextLine();
        List<Tranzakcja> lista = serwis.filtrujPoKategorii(kategoria);
        if (lista.isEmpty()) System.out.println("Brak tranzakcji dla tej kategorii");
        else lista.forEach(System.out::println);
    }

    private void pokazRaportMiesieczny() {
        int rok, miesiac;
        while (true) {
            try {
                System.out.print("Podaj rok (np. 2026): ");
                rok = Integer.parseInt(scanner.nextLine());
                System.out.print("Podaj miesiac (1-12): ");
                miesiac = Integer.parseInt(scanner.nextLine());
                if (miesiac < 1 || miesiac > 12) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("BLAD: Podaj poprawny rok i miesiac");
            }
        }

        double bilans = serwis.bilansMiesieczny(rok, miesiac);
        System.out.println("\n=== RAPORT MIESIECZNY: " + rok + "-" + miesiac + " ===");
        System.out.println("Bilans miesieczny: " + bilans);

        System.out.println("\nPrzychody wedlug kategorii:");
        Map<String, Double> przychody = serwis.sumaWedKategorii(rok, miesiac, "PRZYCHOD");
        if (przychody.isEmpty()) System.out.println("Brak przychodow w tym miesiacu");
        else przychody.forEach((k, v) -> System.out.println(k + ": " + v));

        System.out.println("\nWydatki wedlug kategorii:");
        Map<String, Double> wydatki = serwis.sumaWedKategorii(rok, miesiac, "WYDATEK");
        if (wydatki.isEmpty()) System.out.println("Brak wydatkow w tym miesiacu");
        else wydatki.forEach((k, v) -> System.out.println(k + ": " + v));
    }

    private void eksportRaportu() {
        int rok, miesiac;
        while (true) {
            try {
                System.out.print("Podaj rok (np. 2026): ");
                rok = Integer.parseInt(scanner.nextLine());
                System.out.print("Podaj miesiac (1-12): ");
                miesiac = Integer.parseInt(scanner.nextLine());
                if (miesiac < 1 || miesiac > 12) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("BLAD: Podaj poprawny rok i miesiac");
            }
        }
        serwis.eksportRaportuMiesiecznego(rok, miesiac);
    }
}