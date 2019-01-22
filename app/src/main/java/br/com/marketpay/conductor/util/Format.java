package br.com.marketpay.conductor.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Format {

    public static String getMascaraCartao(String nu_cartao) {
        StringBuilder mascara = new StringBuilder();
        if (nu_cartao != null && !nu_cartao.equals("") & nu_cartao.length() == 16) {
            mascara.append("XXXX XXXX XXXX ");
            mascara.append(nu_cartao.substring(12, 16));
            return mascara.toString();
        }
        return "";
    }

    public static String converteDate(String dataEmTexto) {

        SimpleDateFormat format = new SimpleDateFormat(Constants.FORMATO_DATA);
        Calendar calendar = Calendar.getInstance();
        try{
            Date date = new Date(format.parse(dataEmTexto.replaceAll("T", " ").replaceAll("Z", "")).getTime());
            calendar.setTime(date);
        } catch (ParseException e) {
            Log.e(Constants.LOG_TAG, e.getMessage());
        }
        return format.format(calendar.getTime());
    }

    public static String formatDate(String dataEmTexto) {

        StringBuilder dataFormata = new StringBuilder();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(Constants.FORMATO_DATA);
        Calendar calendar = Calendar.getInstance();

        try{
            Date date = new Date(format.parse(dataEmTexto.replaceAll("T", " ").replaceAll("Z", "")).getTime());
            calendar.setTime(date);

            int mes = calendar.get(Calendar.MONTH);
            int dia = calendar.get(Calendar.DAY_OF_MONTH);
            dataFormata.append(dia).append(" ").append(nomeDoMes(mes));
            return dataFormata.toString();

        } catch (ParseException e) {
            Log.e(Constants.LOG_TAG, e.getMessage());
            return "";
        }
    }

    public static String getAnoAtual() {
        Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        return String.valueOf(ano);
    }

    public static String nomeDoMes(int mes) {
        String meses[] = {"JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"};
        return(meses[mes]);
    }

    public static String nomeDoMesPorExtenso(int mes) {
        String meses[] = {"JANEIRO", "FEVEREIRO", "MARCO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"};
        return(meses[mes]);
    }

    public static String formatMoeda(BigDecimal valor) {
        NumberFormat moeda = DecimalFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return moeda.format(valor).replace("R$", "").replace("-R$ ", "");
    }

    public static String formatMoedaSimbolo(BigDecimal valor) {
        NumberFormat moeda = DecimalFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return moeda.format(valor).replace("R$", "R$ ").replace("-R$ ", "R$ -");
    }

    public static String getMes(){
        Calendar calendar = Calendar.getInstance();
        int mes = calendar.get(Calendar.MONTH);
        return nomeDoMesPorExtenso(mes);
    }
}
