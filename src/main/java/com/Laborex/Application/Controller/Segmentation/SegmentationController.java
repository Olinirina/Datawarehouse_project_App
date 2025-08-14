package com.Laborex.Application.Controller.Segmentation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Laborex.Application.SegmentationDTO.ClassificationACBDTO;
import com.Laborex.Application.SegmentationDTO.ClientRFMDTO;
import com.Laborex.Application.Service.Segmentation.ABCService;

@RestController
@RequestMapping("/api/segmentation")
public class SegmentationController {
	@Autowired
	public ABCService abcService;
	//CLASSIFICATION ARTICLES
	@GetMapping("/abc/articles")
    public ResponseEntity<List<ClassificationACBDTO>> getAbcArticles() {
        return ResponseEntity.ok(abcService.getAbcAnalyseParArticle());
    }
	//CLASSIFICATION CLIENTS
		@GetMapping("/abc/clients")
	    public ResponseEntity<List<ClassificationACBDTO>> getAbcClient() {
	        return ResponseEntity.ok(abcService.getAbcAnalyseParClient());
	    }
	//CLASSIFICATION CLIENTS
	@GetMapping("/abc/labo")
	public ResponseEntity<List<ClassificationACBDTO>> getAbcLabo() {
		return ResponseEntity.ok(abcService.getAbcAnalyseParLabo());
	}
	//SEGMENTATION RFM
	@GetMapping("/rfm")
	public ResponseEntity<List<ClientRFMDTO>> getSegmentationRFM() {
		return ResponseEntity.ok(abcService.getRFMSegmentation());
	}

}
