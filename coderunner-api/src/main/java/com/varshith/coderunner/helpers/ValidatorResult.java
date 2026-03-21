package com.varshith.coderunner.helpers;

import lombok.Setter;

/**
 * Helper class to maintain clean Validator result structure for multiple validation methods, Ex: Using bool, string;
 * bool, path etc.
 * */

public class ValidatorResult<A, B> {
    @Setter private  A firstVariable;
    @Setter private  B secondVariable;

    public ValidatorResult(A firstVariable, B secondVariable) {
        this.firstVariable = firstVariable;
        this.secondVariable = secondVariable;
    }

    public A first(){
        return firstVariable;
    }
    public B second(){
        return secondVariable;
    }
}
