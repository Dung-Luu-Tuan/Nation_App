package com.example.nation_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> list;
    EditText et1;
    EditText et2;
    ArrayAdapter adapter;
    Spinner spinner1;
    Spinner spinner2;
    Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 = (Spinner) findViewById(R.id.from);
        spinner2 = (Spinner) findViewById(R.id.to);
        list = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        new ReadRSS().execute("https://usd.fxexchangerate.com/rss.xml");

        et1 = (EditText) findViewById(R.id.edittext1);
        et2 = (EditText) findViewById(R.id.result);
        bt = (Button) findViewById(R.id.button);
    }

    private class ReadRSS extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                InputStreamReader inputStreamReader = new InputStreamReader(url.openConnection().getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String dataPrice = "";
            String dataTitle = "";
            XMLDOMParser parser = new XMLDOMParser();
            Document document = parser.getDocument(s);
            NodeList nodeList = document.getElementsByTagName("item");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                dataPrice = parser.getValue(element, "description");
                dataTitle = parser.getValue(element, "title");
                String[] parts = dataPrice.split(" ", 9);
                int Position = dataTitle.indexOf("/");
                String NameNation = dataTitle.substring(Position + 1);
                list.add(NameNation);
            }
            adapter.notifyDataSetChanged();
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(et1.getText().toString().isEmpty() && et2.getText().toString().isEmpty()){
                        Toast.makeText(MainActivity.this, "Please enter the number", Toast.LENGTH_SHORT).show();
                    }
                    else if(!isNumeric(et1.getText().toString())){
                        Toast.makeText(MainActivity.this, "Please enter the number", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String itemSelected1 = (String) spinner1.getSelectedItem();
                        String itemSelected2 = (String) spinner2.getSelectedItem();
                        int Position1 = itemSelected1.indexOf("(");
                        Double DataBeforeConvert = Double.parseDouble(String.valueOf(et1.getText()));
                        Double DataAfterConvert = 0.0;
                        String Title1 = itemSelected1.substring(Position1 + 1, itemSelected1.length() - 1);
                        String Title2 = itemSelected2.substring(0, itemSelected2.length() - 5);
                        new ReadRSS().execute("https://" + Title1 + ".fxexchangerate.com/rss.xml");
                        XMLDOMParser parser = new XMLDOMParser();
                        Document document = parser.getDocument(s);
                        NodeList nodeList = document.getElementsByTagName("item");
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Element element = (Element) nodeList.item(i);
                            String data = parser.getValue(element, "description");
                            String[] parts = data.split(" ", 9);
                            if (itemSelected1.equals(itemSelected2)) {
                                DataAfterConvert = DataBeforeConvert;
                            } else if (data.contains(Title2)) {
                                for (int k = 0; k < parts.length; k++) {
                                    if (parts[k].equals("=")) {
                                        DataAfterConvert = DataBeforeConvert * (Double.parseDouble(parts[k + 1]));
                                    }
                                }
                            }
                        }
                        Toast.makeText(MainActivity.this, String.valueOf(DataAfterConvert), Toast.LENGTH_SHORT).show();
                        et2.setText(String.valueOf(DataAfterConvert));
                    }
                }
            });
            adapter.notifyDataSetChanged();
        }
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}