package finanse.serwis;

import finanse.model.Tranzakcja;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SerwisTranzakcji {

    private final List<Tranzakcja> listaTranzakcji;
    private final String NAZWA_PLIKU = "tranzakcje.csv";

    public SerwisTranzakcji() {
        this.listaTranzakcji = new ArrayList<>();
    }


    public void dodajTranzakcje(Tranzakcja t) { listaTranzakcji.add(t); }
    public List<Tranzakcja> wszystkieTranzakcje() { return listaTranzakcji; }

    public List<Tranzakcja> filtrujPoTypie(String typ) {
        return listaTranzakcji.stream()
                .filter(t -> t.typ().equalsIgnoreCase(typ))
                .collect(Collectors.toList());
    }

    public List<Tranzakcja> filtrujPoKategorii(String kategoria) {
        return listaTranzakcji.stream()
                .filter(t -> t.kategoria().equalsIgnoreCase(kategoria))
                .collect(Collectors.toList());
    }

    public double obliczBilans() {
        double przychody = listaTranzakcji.stream()
                .filter(t -> t.typ().equalsIgnoreCase("PRZYCHOD"))
                .mapToDouble(Tranzakcja::kwota).sum();
        double wydatki = listaTranzakcji.stream()
                .filter(t -> t.typ().equalsIgnoreCase("WYDATEK"))
                .mapToDouble(Tranzakcja::kwota).sum();
        return przychody - wydatki;
    }


    public void zapiszDoPliku() {
        try (java.io.FileWriter fw = new java.io.FileWriter(NAZWA_PLIKU, false);
             java.io.PrintWriter writer = new java.io.PrintWriter(fw)) {
            for (Tranzakcja t : listaTranzakcji) {
                writer.println(t.kwota() + "," + t.typ() + "," + t.kategoria() + "," +
                        t.data() + "," + t.opis());
            }
            System.out.println("Dane zapisane do pliku: " + NAZWA_PLIKU);
        } catch (Exception e) {
            System.out.println("BLAD przy zapisie do pliku: " + e.getMessage());
        }
    }

    public void wczytajZPliku() {
        java.io.File plik = new java.io.File(NAZWA_PLIKU);
        if (!plik.exists()) {
            System.out.println("Plik nie istnieje. Brak danych do wczytania");
            return;
        }
        listaTranzakcji.clear();

        try (java.util.Scanner fileScanner = new java.util.Scanner(plik)) {
            while (fileScanner.hasNextLine()) {
                String linia = fileScanner.nextLine();
                String[] pola = linia.split(",", 5);
                if (pola.length == 5) {
                    double kwota = Double.parseDouble(pola[0]);
                    String typ = pola[1];
                    String kategoria = pola[2];
                    LocalDate data = LocalDate.parse(pola[3]);
                    String opis = pola[4];
                    Tranzakcja t = new Tranzakcja(kwota, typ, kategoria, data, opis);
                    listaTranzakcji.add(t);
                }
            }
            System.out.println("Dane wczytane z pliku: " + NAZWA_PLIKU);
        } catch (Exception e) {
            System.out.println("BLAD przy wczytywaniu z pliku: " + e.getMessage());
        }
    }

    public double bilansMiesieczny(int rok, int miesiac) {
        YearMonth ym = YearMonth.of(rok, miesiac);
        double przychody = listaTranzakcji.stream()
                .filter(t -> t.typ().equalsIgnoreCase("PRZYCHOD"))
                .filter(t -> YearMonth.from(t.data()).equals(ym))
                .mapToDouble(Tranzakcja::kwota).sum();

        double wydatki = listaTranzakcji.stream()
                .filter(t -> t.typ().equalsIgnoreCase("WYDATEK"))
                .filter(t -> YearMonth.from(t.data()).equals(ym))
                .mapToDouble(Tranzakcja::kwota).sum();

        return przychody - wydatki;
    }

    public Map<String, Double> sumaWedKategorii(int rok, int miesiac, String typ) {
        YearMonth ym = YearMonth.of(rok, miesiac);
        return listaTranzakcji.stream()
                .filter(t -> t.typ().equalsIgnoreCase(typ))
                .filter(t -> YearMonth.from(t.data()).equals(ym))
                .collect(Collectors.groupingBy(
                        Tranzakcja::kategoria,
                        Collectors.summingDouble(Tranzakcja::kwota)
                ));
    }

    public void eksportRaportuMiesiecznego(int rok, int miesiac) {
        String nazwaPlikuRaportu = "raport_" + rok + "-" + String.format("%02d", miesiac) + ".csv";
        try (java.io.PrintWriter writer = new java.io.PrintWriter(nazwaPlikuRaportu)) {
            writer.println("Bilans miesieczny," + bilansMiesieczny(rok, miesiac));

            writer.println("\nPrzychody według kategorii:");
            Map<String, Double> przychody = sumaWedKategorii(rok, miesiac, "PRZYCHOD");
            for (String kategoria : przychody.keySet()) {
                writer.println(kategoria + "," + przychody.get(kategoria));
            }

            writer.println("\nWydatki według kategorii:");
            Map<String, Double> wydatki = sumaWedKategorii(rok, miesiac, "WYDATEK");
            for (String kategoria : wydatki.keySet()) {
                writer.println(kategoria + "," + wydatki.get(kategoria));
            }

            System.out.println("Raport zapisany do pliku: " + nazwaPlikuRaportu);
        } catch (Exception e) {
            System.out.println("BLAD przy zapisie raportu: " + e.getMessage());
        }
    }
}