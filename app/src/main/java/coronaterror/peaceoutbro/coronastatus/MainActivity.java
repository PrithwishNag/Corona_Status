package coronaterror.peaceoutbro.coronastatus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    Elements totalcases;
    String drateString;
    String rrateString;
    Double drate;
    Double rrate;
    Element activeclosed;
    Element recoveredDead;
    Element mildcrit;
    String[] info;
    String[] acinfo = new String[2];
    String[] mcinfo = new String[2];
    String[] percentmildcrit = new String[2];
    String[] rdinfo = new String[2];
    String[] percentrecdeath = new String[2];
    String []countrynamearr;
    int rowcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton infobut = findViewById(R.id.info);
        infobut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInfo();
            }
        });

        if(!isOnline())
        {
            Toast toast = Toast.makeText(getApplicationContext(), "You Are Offline!!!", Toast.LENGTH_LONG);
            toast.show();
        }

        new Scrapper().execute();

        ImageButton sync = findViewById(R.id.sync);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });
    }

    public void ShowInfo()
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.alert_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        Button dbutton = dialog.findViewById(R.id.button);
        dbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public boolean isOnline() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
            return true;
        }
        else{
            return false;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Scrapper extends AsyncTask<Void, Void, Void> {
        AlertDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = new ProgressDialog(MainActivity.this);
            //progressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String url = "https://www.worldometers.info/coronavirus/";

                Document document = Jsoup.connect(url).get();

                MainData(document);

                ActiveClosedData(document);

                countryData(document);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            printResults();
            createTable();
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void printResults() {
        TextView TotalCases = findViewById(R.id.totalcases);
        TextView DeathCases = findViewById(R.id.deaths);
        TextView RecoveredCases = findViewById(R.id.recovered);
        TextView ActiveCases = findViewById(R.id.active);
        TextView MildCases = findViewById(R.id.mild);
        TextView CriticalCases = findViewById(R.id.critical);
        TextView ClosedCases = findViewById(R.id.closed);
        TextView RecoveredSub = findViewById(R.id.recoveredsub);
        TextView DeathSub = findViewById(R.id.deathsub);
        TextView RecoveryRate = findViewById(R.id.recoveryrate);
        TextView DeathRate = findViewById(R.id.deathrate);


        if (info != null) {
            TotalCases.setTextSize(40f);
            DeathCases.setTextSize(40f);
            RecoveredCases.setTextSize(40f);
            DeathCases.setTextColor(getColor(R.color.Red));
            RecoveredCases.setTextSize(40f);
            RecoveredCases.setTextColor(getColor(R.color.colorAccent));

            TotalCases.setText(info[0]);
            DeathRate.setText("[" + drateString + "%]");
            DeathCases.setText(info[1]);
            RecoveryRate.setText("[" + rrateString + "%]");
            RecoveredCases.setText(info[2]);
        }

        if (acinfo != null) {
            ActiveCases.setText("Active Cases\n" + acinfo[0]);
            ClosedCases.setText("Closed Cases\n" + acinfo[1]);
        }
        else
        {
            ActiveCases.setText("Active Cases\n");
            ClosedCases.setText("Closed Cases\n");
        }
        if (mcinfo != null && percentmildcrit != null) {
            MildCases.setText("Mild Cases\n" + mcinfo[0] + " [" + percentmildcrit[0] + "%]");
            CriticalCases.setText("Critical Cases\n" + mcinfo[1] + " [" + percentmildcrit[1] + "%]");
        }
        else
        {
            MildCases.setText("Mild Cases\n");
            CriticalCases.setText("Critical Cases\n");
        }

        if (rdinfo != null && percentrecdeath != null) {
            RecoveredSub.setText("Recovered Cases\n" + rdinfo[0] + " [" + percentrecdeath[0] + "%]");
            DeathSub.setText("Death Cases\n" + rdinfo[1] + " [" + percentrecdeath[1] + "%]");
        }
        else
        {
            RecoveredSub.setText("Recovered Cases\n");
            DeathSub.setText("Death Cases\n");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void MainData(Document document)
    {
        totalcases = document.getElementsByClass("maincounter-number");

        info = totalcases.text().split(" ");
        //Log.d("gg","info-->"+totalcases.text());

        int t = 1, r = 0, d = 0;
        for (int j = 0; j < info.length; j++) {
            String[] tx = info[j].split(",");
            String joined = "";
            int n = tx.length;

            for (int i = 0; i < n; i++) {
                joined = joined.concat(tx[i]);
            }
            if (j == 0)
                t = Integer.parseInt(joined);
            if (j == 1)
                d = Integer.parseInt(joined);
            if (j == 2)
                r = Integer.parseInt(joined);
        }

        DecimalFormat df = new DecimalFormat("###.#");
        drate = (double) (d * 100) / t;
        drateString = df.format(drate);
        rrate = (double) (r * 100) / t;
        rrateString = df.format(rrate);
    }

    public void ActiveClosedData(Document document)
    {
        activeclosed = document.getElementsByClass("number-table-main").get(0);
        acinfo[0] = activeclosed.text();
        mildcrit = document.getElementsByClass("number-table").get(0);
        mcinfo[0] = mildcrit.text();
        percentmildcrit[0] = mildcrit.nextElementSibling().text();

        mildcrit = document.getElementsByClass("number-table").get(1);
        mcinfo[1] = mildcrit.text();
        percentmildcrit[1] = mildcrit.nextElementSibling().text();

        recoveredDead = document.getElementsByClass("number-table-main").get(1);
        acinfo[1] = recoveredDead.text();
        mildcrit = document.getElementsByClass("number-table").get(2);
        rdinfo[0] = mildcrit.text();
        percentrecdeath[0] = mildcrit.nextElementSibling().text();

        mildcrit = document.getElementsByClass("number-table").get(3);
        rdinfo[1] = mildcrit.text();
        percentrecdeath[1] = mildcrit.nextElementSibling().text();
    }

    String []totalcasestablearr;
    String []deathcasestablearr;
    String []newcasestablearr;
    void countryData(Document document)
    {
        int i=0;
        Elements CountryNamesEle = document.select("#main_table_countries_today > tbody > tr ");

        for(Element x:CountryNamesEle)
        {
            i++;
        }

        rowcount=i;
        Log.d("gg","--->>"+rowcount);
        countrynamearr = new String[i+1];
        newcasestablearr =  new String[i+1];
        totalcasestablearr =  new String[i+1];
        deathcasestablearr =  new String[i+1];

        i=0;
        for(Element x: CountryNamesEle)
        {
            //Log.d("gg","Country--->>"+x.child(0).text());
            //Log.d("gg","num--->>"+x.child(1).text());
            countrynamearr[i] = x.child(0).text();
            totalcasestablearr[i] = x.child(1).text();
            newcasestablearr[i] = x.child(2).text();
            deathcasestablearr[i] = x.child(3).text();
            i++;
        }

    }

    Context context=this;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createTable()
    {
        LinearLayout ll = findViewById(R.id.mainlayout);

        //Parameters
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        //TableLayout
        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));// assuming the parent view is a LinearLayout
        tableLayout.setBackgroundResource(R.drawable.active_background);
        tableLayout.setPadding(20,20,20,20);

        //Headings
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(tableParams);

        TextView countrycol = new TextView(context);
        countrycol.setLayoutParams(rowParams);
        countrycol.setText("COUNTRIES");
        countrycol.setTextSize(15f);
        countrycol.setPadding(0,0,20,20);

        TextView newcasescol = new TextView(context);
        newcasescol.setLayoutParams(rowParams);
        newcasescol.setText("NEW\nCASES\t");
        newcasescol.setTextSize(15f);
        newcasescol.setPadding(0,0,20,20);

        TextView totalcasescol = new TextView(context);
        totalcasescol.setLayoutParams(rowParams);
        totalcasescol.setText("TOTAL\nCASES\t");
        totalcasescol.setTextSize(15f);
        totalcasescol.setPadding(0,0,20,20);

        TextView deathcasescol = new TextView(context);
        deathcasescol.setLayoutParams(rowParams);
        deathcasescol.setText("NOT\nRECOVERED\t");
        deathcasescol.setTextSize(15f);
        deathcasescol.setPadding(0,0,0,20);

        tableRow.addView(countrycol);
        tableRow.addView(newcasescol);
        tableRow.addView(totalcasescol);
        tableRow.addView(deathcasescol);
        tableLayout.addView(tableRow);

        int n = rowcount;
        for(int i=0;i<n;i++) {
            //Tablerow
            tableRow = new TableRow(context);
            tableRow.setLayoutParams(tableParams);// TableLayout is the parent view
            tableRow.setBackgroundResource(R.drawable.table_row_design);
            tableRow.setPadding(0,5,0,5);

            //TextViews in Rows
            if(countrynamearr!=null) {
                countrycol = new TextView(context);
                countrycol.setLayoutParams(rowParams);// TableRow is the parent view
                countrycol.setText(countrynamearr[i]);
                countrycol.setPadding(20,0,20,0);
                countrycol.setTextSize(15f);
            }

            if(newcasestablearr!=null) {
                newcasescol = new TextView(context);
                newcasescol.setLayoutParams(rowParams);// TableRow is the parent view
                newcasescol.setTextColor(getColor(R.color.brown_yellow));
                newcasescol.setText(newcasestablearr[i]);
            }

            if(totalcasestablearr!=null) {
                totalcasescol = new TextView(context);
                totalcasescol.setLayoutParams(rowParams);// TableRow is the parent view
                totalcasescol.setText(totalcasestablearr[i]);
            }

            if(deathcasestablearr!=null) {
                deathcasescol = new TextView(context);
                deathcasescol.setLayoutParams(rowParams);// TableRow is the parent view
                deathcasescol.setText(deathcasestablearr[i]);
            }
            //Adding Table->LinearLayout TableRow->Table TextView->TableRow
            tableRow.addView(countrycol);
            tableRow.addView(newcasescol);
            tableRow.addView(totalcasescol);
            tableRow.addView(deathcasescol);
            tableLayout.addView(tableRow);
        }
        ll.addView(tableLayout);
    }
}
