package com.example.entrega1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baraja {
    private List<String> mazo;
    private List<String> manoDealer;
    private List<String> manoJugador;

    private static final String[] PALOS = {"c", "d", "h", "s"}; //clovers, diamonds, hearts and spades
    private static final String[] VALORES = {"a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k"};


    public Baraja() {
        mazo = new ArrayList<>();
        manoDealer = new ArrayList<>();
        manoJugador = new ArrayList<>();

        this.inicializarMazo();
    }

    public void inicializarMazo(){
        List<String> m = this.getMazo();
        for (String p : PALOS) {
            for (String v : VALORES) {
                m.add(p + v);
            }
        }
    }

    public void barajar(){
        Collections.shuffle(this.mazo);
    }

    public boolean repartirJugador(){
        if (!mazo.isEmpty()) {
            String carta = mazo.get(0);
            mazo.remove(0);
            manoJugador.add(carta);
            return true;
        } else {
            return false;
        }
    }

    public boolean repartirDealer(){
        if (!mazo.isEmpty()) {
            String carta = mazo.get(0);
            mazo.remove(0);
            manoDealer.add(carta);
            return true;
        } else {
            return false;
        }
    }

    public int[] calcMano(List<String> mano){
        int total = 0;
        int total2 = 0;
        //int total3 = 0; //innecesarios, porque si cuentas un as como 11, el resto deben contarse como uno por motivos numéricos
        //int total4 = 0;
        int numAses = 0;
        for(String c : mano){

            switch(c.substring(1)){
                case "a":
                    //casuisticas de múltiples ases en la mano (improbable, no imposible)
                    if (numAses == 0) {
                        total2 = total + 11;
                    } else {
                        total2 = total2 + 1;
                    }
                    total = total + 1;
                    numAses = numAses + 1;
                    break;
                case "j":
                case "q":
                case "k":
                    total = total + 10;
                    total2 = total2 + 10;

                    break;
                default:
                    int p = Integer.parseInt(c.substring(1));
                    total = total + p;
                    total2 = total2 + p;
            }

        }

        return new int[]{total,total2,numAses};
    }

    public void vaciarMano(List<String> mano){
        while(!mano.isEmpty()){
            mazo.add(mano.get(0));
            mano.remove(0);
        }
    }

    public List<String> getMazo(){
        return this.mazo;
    }

    public List<String> getManoDealer(){
        return this.manoDealer;
    }

    public List<String> getManoJugador(){
        return this.manoJugador;
    }

}
