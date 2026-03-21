package com.varshith.coderunner.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Model for Language contains language name and language ID and language multiplier which is used for changing
 * time limits dynamically based on language, Ex: Cpp:1.0, Java:1.5, Python:2.0 etc.
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LanguageModel {

    @Id
    @Column(name="languageId")
    private int languageId;

    private String languageName;

    private double languageMultiplier;
}