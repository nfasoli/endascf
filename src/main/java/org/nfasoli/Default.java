package org.nfasoli;


public class Default {
    public String indirizzo;
    public String comune;
    public String CAP;
    public String provincia;
    public String email;
    public String telefono;
    public String cellulare;

    Default(String indirizzo, String comune, String CAP, String provincia, String email, String telefono, String cellulare)
    {
        this.indirizzo = indirizzo;
        this.comune = comune;
        this.CAP = CAP;
        this.provincia = provincia;
        this.email = email;
        this.telefono = telefono;
        this.cellulare = cellulare;
    }

    Default()
    {
        this.indirizzo = "";
        this.comune = "";
        this.CAP = "";
        this.provincia = "";
        this.email = "";
        this.telefono = "";
        this.cellulare = "";
    }

}
