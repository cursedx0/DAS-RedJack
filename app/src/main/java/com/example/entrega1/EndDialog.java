package com.example.entrega1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.Nullable;

public class EndDialog extends DialogFragment {

    private int result;
    private int saldo; //nuevo saldo
    private int apuesta;
    private String msg;
    private String title;

    public EndDialog(){

    }

    public static EndDialog newIntance(int result, int saldo, int apuesta){
        /*this.result = result;
        this.saldo = saldo;
        this.apuesta = apuesta;*/
        EndDialog myEndDialog = new EndDialog();
        myEndDialog.result=result;
        myEndDialog.saldo=saldo;
        myEndDialog.apuesta=apuesta;
        return myEndDialog;
    }

    /*dialogListener miListener;
    public interface dialogListener {
        void alpulsarSI();
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        //miListener = (dialogListener) getActivity();

        if(result==1){
            msg = getString(R.string.victoriaMsg, apuesta, saldo);
            title = getString(R.string.hasganado);
        }else if(result==0){
            msg = getString(R.string.derrotaMsg, apuesta, saldo);
            title = getString(R.string.hasperdido);
        }else{
            msg = getString(R.string.hasempatado);
            title = getString(R.string.empate);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }
}