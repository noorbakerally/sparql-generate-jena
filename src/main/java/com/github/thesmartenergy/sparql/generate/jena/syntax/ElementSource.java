/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thesmartenergy.sparql.generate.jena.syntax;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * The {@code SOURCE} clause.
 * 
 * @author maxime.lefrancois
 */
public class ElementSource extends ElementIteratorOrSource {

    private final Node source, accept;

    public ElementSource(Node source, Node accept, Var var) {
        super(var);
        this.source = source;
        this.accept = accept;
    }

    public Node getSource() {
        return source;
    }
    
    public Node getAccept() {
        return accept;
    }    
    
    @Override
    public void visit(ElementVisitor v) {
        if (v instanceof SPARQLGenerateElementVisitor) {
            ((SPARQLGenerateElementVisitor) v).visit(this);
        }
    }
    
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
