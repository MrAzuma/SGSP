package com.example.ruan.sgsp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    EditText login, senha;
    String res= "" ;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (EditText)findViewById(R.id.txt_user);
        senha = (EditText)findViewById(R.id.txt_senha);
    }

    //criando uma array com os valores a serem enviados
    public List<Formulario> listadados(Formulario formulario){
        List<Formulario>lista = new ArrayList<>();
        formulario.setLogin(login.getText().toString());
        formulario.setSenha(senha.getText().toString());
        lista.add(formulario);
        return lista;
    }

    //metodo que gera o Objeto e Array json.
    public void abrir_teste(View v) {

        if(login.getText().toString().equals("") || senha.getText().toString().equals("")){
            Toast.makeText(this, "Há campos a serem preenchidos", Toast.LENGTH_SHORT).show();
            return;
        }
        enviar_dados();

    }

    private void enviar_dados(){
        String json = gerarJSON_formulario();
        new ConexaoHTTP().execute(json);

    }

    //valida resposta ao tentar se autenticar
    private void valida_res(){

        if(res.equals("Ok")){
            Toast.makeText(this,"Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, FormularioActivity.class);
            String txt = "";
            txt = login.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString("txt", txt);
            intent.putExtras(bundle);
            startActivity(intent);
        }else{
            Toast.makeText(this,""+res, Toast.LENGTH_SHORT).show();
        }
    }

    //Gera um json com as credenciais do professor
    private String gerarJSON_formulario(){
        List<Formulario>lista = listadados(new Formulario());
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try{
            JSONObject formularioJSON = new JSONObject();
            formularioJSON.put("login",lista.get(0).getLogin());
            formularioJSON.put("senha",lista.get(0).getSenha());
            jsonArray.put(formularioJSON);
            jsonObject.put("formulario",jsonArray);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    //conexão http com o arquivo de autenticação
    private class ConexaoHTTP extends AsyncTask<String,Void,String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(LoginActivity.this);
            progress.setMessage("Autenticando...");
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String resposta = "";
            String dados = params[0];
            InputStream input = null;
            HttpURLConnection conexao = null;
            StringBuilder resultado = null;
            try {
                //Enviando Json usando POST
                URL urlCon = new URL("http://10.114.50.231/mobile/servicedoid_autentica.php");
                //URL urlCon = new URL("https://acessonet.000webhostapp.com/servicedoid_autentica.php");
                //URL urlCon = new URL("http://192.168.1.3/servicedoid_autentica.php");
                conexao = (HttpURLConnection) urlCon.openConnection();
                conexao.setReadTimeout(10000);
                conexao.setConnectTimeout(15000);
                conexao.setRequestMethod("POST");
                conexao.setDoInput(true);
                conexao.setDoOutput(true);

                conexao.setRequestProperty("Content-Type", "application/json");
                conexao.setRequestProperty("Accept", "application/json");

                conexao.connect();

                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(conexao.getOutputStream()));
                output.write(dados);
                output.close();

                //Recebendo resposta do servidor

                int codigo = conexao.getResponseCode();

                if (codigo == HttpURLConnection.HTTP_OK) {

                    input = new BufferedInputStream(conexao.getInputStream());
                    BufferedReader leitor = new BufferedReader(new InputStreamReader(input));
                    resultado = new StringBuilder();
                    String linha;
                    while ((linha = leitor.readLine()) != null) {
                        resultado.append(linha);
                    }
                    resposta = resultado.toString();

                } else {
                    resposta = "Falha ao estabelecer conexão com o servidor!";
                }


            } catch (IOException e) {
                e.printStackTrace();
                resposta = "Falha na conexão com o servidor!";
            }

            conexao.disconnect();

            return resposta;
        }

        @Override
        protected void onPostExecute(String pres) {
            res = pres;
            progress.dismiss();
            valida_res();
        }
    }
}


