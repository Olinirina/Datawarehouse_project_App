package com.Laborex.Application.KPI.DTO;

import java.util.List;

import com.Laborex.Application.KPI.DTO.CA.CaParArticleDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParClientDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParLaboDTO;
import com.Laborex.Application.KPI.DTO.CA.CaParPeriodeDTO;

public class CaVenteDTO {
	private double caGlobal;
    private List<CaParClientDTO> caParClient;
    private List<CaParArticleDTO> caParArticle;
    private List<CaParLaboDTO> caParLabo;
    private List<CaParPeriodeDTO> caParPeriode;
	public CaVenteDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaVenteDTO(double caGlobal, List<CaParClientDTO> caParClient, List<CaParArticleDTO> caParArticle,
			List<CaParLaboDTO> caParLabo, List<CaParPeriodeDTO> caParPeriode) {
		super();
		this.caGlobal = caGlobal;
		this.caParClient = caParClient;
		this.caParArticle = caParArticle;
		this.caParLabo = caParLabo;
		this.caParPeriode = caParPeriode;
	}
	public double getCaGlobal() {
		return caGlobal;
	}
	public void setCaGlobal(double caGlobal) {
		this.caGlobal = caGlobal;
	}
	public List<CaParClientDTO> getCaParClient() {
		return caParClient;
	}
	public void setCaParClient(List<CaParClientDTO> caParClient) {
		this.caParClient = caParClient;
	}
	public List<CaParArticleDTO> getCaParArticle() {
		return caParArticle;
	}
	public void setCaParArticle(List<CaParArticleDTO> caParArticle) {
		this.caParArticle = caParArticle;
	}
	public List<CaParLaboDTO> getCaParLabo() {
		return caParLabo;
	}
	public void setCaParLabo(List<CaParLaboDTO> caParLabo) {
		this.caParLabo = caParLabo;
	}
	public List<CaParPeriodeDTO> getCaParPeriode() {
		return caParPeriode;
	}
	public void setCaParPeriode(List<CaParPeriodeDTO> caParPeriode) {
		this.caParPeriode = caParPeriode;
	}
    

}
