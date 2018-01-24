package com.example.ruan.sgsp;

/**
 * Created by Ruan on 11/05/2017.
 */
import java.io.Serializable;

public class Formulario implements Serializable {
    private String prontuario;
    private String msg;
    private String responsavel;
    private String login;
    private String senha;
    private String area;

    public String getLogin(){
        return login;
    }
    public void setLogin(String login){
        this.login=login;
    }
    public String getSenha(){
        return senha;
    }
    public void setSenha(String senha){
        this.senha=senha;
    }
    public String getProntuario(){
        return prontuario;
    }
    public void setProntuario(String prontuario) {
        this.prontuario = prontuario;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getResponsavel() {
        return responsavel;
    }
    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }
    public String getArea(){
        return area;
    }
    public void setArea(String area){
        this.area = area;
    }
}
