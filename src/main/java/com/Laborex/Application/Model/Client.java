package com.Laborex.Application.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Client {
	@Id
	private String codeCli;
	private String nomCli;
	public Client() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Client(String codeCli, String nomCli) {
		super();
		this.codeCli = codeCli;
		this.nomCli = nomCli;
	}
	public String getCodeCli() {
		return codeCli;
	}
	public void setCodeCli(String codeCli) {
		this.codeCli = codeCli;
	}
	public String getNomCli() {
		return nomCli;
	}
	public void setNomCli(String nomCli) {
		this.nomCli = nomCli;
	}
	

}
