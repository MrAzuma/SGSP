package com.example.ruan.sgsp;
//Importações basicas do app
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class FormularioActivity extends Activity {
    public static final String myPREFERENCES = "SESSAO";
    EditText motivo, prontuario, nome, semestre, curso, nivel;
    AlertDialog alerta;
    String responsavel="",res_envio="", area="";
    RadioButton rbt_aberto, rbt_socio, rbt_psicologo,  rbt_social;
    RadioGroup rbGroup;
    int erro;

    //passando os valores que estiverem na interface para as variaveis
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        motivo = (EditText) findViewById(R.id.txt_motivo);
        prontuario = (EditText) findViewById(R.id.txt_user);
        semestre = (EditText) findViewById(R.id.txt_ano);
        curso = (EditText) findViewById(R.id.txt_curso);
        nome = (EditText) findViewById(R.id.txt_nome);
        nivel = (EditText) findViewById(R.id.txt_nivel);
        rbGroup = (RadioGroup) findViewById(R.id.rbt_group);
        rbt_aberto = (RadioButton) findViewById(R.id.rbt_aberto);
        rbt_socio = (RadioButton) findViewById(R.id.rbt_socio);
        rbt_social = (RadioButton) findViewById(R.id.rbt_social);
        rbt_psicologo = (RadioButton) findViewById(R.id.rbt_psicologo);
        Intent it= getIntent();
        Bundle bundle = it.getExtras();
        String txt = bundle.getString("txt");
        responsavel = txt;
    }

    //Função do radiobutton da area destino.
    public void onClickRadio(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()){
            case R.id.rbt_aberto:
                if(checked){
                    area = rbt_aberto.getText().toString();
                }
                break;
            case R.id.rbt_social:
                if(checked){
                    area = rbt_social.getText().toString();
                }
                break;
            case R.id.rbt_psicologo:
                if(checked){
                    area=rbt_psicologo.getText().toString();
                }
                break;
            case R.id.rbt_socio:
                if(checked){
                    area = rbt_socio.getText().toString();
                }
                break;
        }
    }

    //metodo que efetua a conexao http de busca gerando um formulario que até aqui contém sómente a opção e o prontuario
    public void busca(View view){
        if (prontuario.getText().toString().equals("")) {
            Toast.makeText(this, "Digite o prontuario!", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            enviarDados(2);
        }
    }

    //ao pressionar o button voltar do aparelho
    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = encerrarSessao();
        alertDialog.show();
    }

    //metodo que executa a conexao http para enviar o formulario final
    private void enviarDados(int op_local){
        try{
            int op=op_local;
            switch(op){
                case 1:
                    new ConexaoHTTP_enviar().execute(gerarJSON_formulario());
                    break;
                case 2:
                    new ConexaoHTTP_buscar().execute(prontuario.getText().toString());
                    break;
            }
        }catch(Exception e){
            Toast.makeText(this, "Aguarde, sistema não respondendo."+e, Toast.LENGTH_SHORT).show();
            return;
        }

    }

    //lista de informação do formulario para adicionar ao json
    private List<Formulario> listadados(Formulario formulario){
        List<Formulario>lista = new ArrayList<>();
        formulario.setProntuario(prontuario.getText().toString());
        formulario.setResponsavel(responsavel);
        formulario.setArea(area);
        formulario.setMsg(motivo.getText().toString());
        lista.add(formulario);
        return lista;
    }

    // gera o json com a lista de informação do formulario
    private String gerarJSON_formulario(){
        List<Formulario>lista = listadados(new Formulario());
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try{
            JSONObject formularioJSON = new JSONObject();
            formularioJSON.put("prontuario",lista.get(0).getProntuario());
            formularioJSON.put("motivo",lista.get(0).getMsg());
            formularioJSON.put("responsavel",lista.get(0).getResponsavel());
            formularioJSON.put("area",lista.get(0).getArea());
            jsonArray.put(formularioJSON);
            jsonObject.put("formulario",jsonArray);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    //metodo que cria um dialogo de encerrar sessao
    private AlertDialog encerrarSessao() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sessão");
        builder.setMessage("Deseja encerrar a sessão ?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sp = getApplicationContext().getSharedPreferences(myPREFERENCES, 0);
                sp.edit().clear().commit();
                finish();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alerta.dismiss();
            }
        });
        alerta = builder.create();
        return alerta;
    }

    //Encerra a sessão ao enviar
    private void fimEnvio(){
        SharedPreferences sp = getApplicationContext().getSharedPreferences(myPREFERENCES, 0);
        sp.edit().clear().commit();
        finish();
    }

    //metodo que sincroniza o enviar com o progress bar de encaminhamento de formulario para webservice.
    public void sincronizar(View view){
        if (
                prontuario.getText().toString().equals("") ||
                        motivo.getText().toString().equals("") ||
                        semestre.getText().toString().equals("") ||
                        nivel.getText().toString().equals("") ||
                        nome.getText().toString().equals(""))
        {
            Toast.makeText(this, "há campos a serem preenchidos !", Toast.LENGTH_SHORT).show();
            return;
        } else {
            try {
                AlertDialog alertDialog = sincronizarDados();
                alertDialog.show();
            } catch (Exception e) {
                Toast.makeText(this, "Erro no meio do processo! " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    //caixa de dialogo do metodo sincronizar
    private AlertDialog sincronizarDados() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Formulario");
        builder.setMessage("O relato sera enviado pelo responsavel: " +responsavel+" para "+area+
                "\nDeseja continuar ?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enviarDados(1);
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alerta.dismiss();
            }
        });
        alerta = builder.create();
        return alerta;
    }

    //Metodo de decodificação do Json
    private void decode_json(String res){
        try {
            JSONObject json = new JSONObject(res);
            nome.setText(json.getString("N_A"));
            curso.setText(json.getString("N_C"));
            semestre.setText(json.getString("DATA_CADASTRO"));
            nivel.setText(json.getString("NIVEL"));
            Toast.makeText(FormularioActivity.this, "Dados do aluno carregados com sucesso!", Toast.LENGTH_SHORT).show();
        }catch(Exception ex){
            Toast.makeText(FormularioActivity.this, "Verifique sua conexão com a internet!", Toast.LENGTH_SHORT).show();
        }
    }

    //conexão http com o arquivo de busca
    private class ConexaoHTTP_buscar extends AsyncTask<String,Void,String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(FormularioActivity.this);
            progress.setMessage("Buscando...");
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
                URL urlCon = new URL("http://10.114.50.231/mobile/servicedoid_busca.php");
                //URL urlCon = new URL("https://acessonet.000webhostapp.com/servicedoid_busca.php");
                //URL urlCon = new URL ("http://192.168.1.3/servicedoid_busca.php");
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
                    resposta = "Falha na conexão com o servidor!";
                }
            } catch (IOException e) {
                e.printStackTrace();
                resposta = "Falha na conexão com o servidor!";
            }
            conexao.disconnect();
            return resposta;
        }

        @Override
        protected void onPostExecute(String resposta) {
            String resp = resposta;
            decode_json(resp);
            progress.dismiss();
        }
    }

    //conexão http com o arquivo de inserção
    private class ConexaoHTTP_enviar extends AsyncTask<String,Void,String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(FormularioActivity.this);
            progress.setMessage("Enviando...");
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
                //URL urlCon = new URL("https://acessonet.000webhostapp.com/servicedoid_envia.php");
                URL urlCon = new URL("http://10.114.50.231/mobile/servicedoid_envia.php");
                //URL urlCon = new URL ("http://192.168.1.3/servicedoid_envia.php");
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

                    erro=0;
                } else {
                    resposta = "Falha ao estabelecer conexão com o servidor!";
                    erro=1;
                }


            } catch (IOException e) {
                e.printStackTrace();
                resposta = "Falha na conexão com o servidor!";
                erro=1;
            }

            conexao.disconnect();
            return resposta;
        }

        @Override
        protected void onPostExecute(String resposta) {
            res_envio = resposta;
            Toast.makeText(FormularioActivity.this, resposta, Toast.LENGTH_SHORT).show();
            progress.dismiss();
            if(erro!=1){
                fimEnvio();
            }
        }
    }

}