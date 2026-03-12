package com.varshith.coderunner.helpers;

import lombok.Setter;


public class Pair<A, B> {
    @Setter private  A firstVariable;
    @Setter private  B secondVariable;

    public Pair(A firstVariable, B secondVariable) {
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
