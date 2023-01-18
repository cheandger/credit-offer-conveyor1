package com.shrek.controllers;
import com.shrek.controller.OffersServiceApi;

import com.shrek.model.LoanApplicationRequestDTO;
import com.shrek.model.LoanOfferDTO;
import com.shrek.servises.OffersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated


public class OffersServiceController implements OffersServiceApi {

        private final OffersService offersService;

    public OffersServiceController(OffersService offersService) {

        this.offersService = offersService;
    }

    @Override
    public ResponseEntity<List<LoanOfferDTO>> offers (@RequestParam LoanApplicationRequestDTO loanApplicationRequestDTO)  {

        return ResponseEntity.ok(offersService.offers(loanApplicationRequestDTO));
        }
    }

