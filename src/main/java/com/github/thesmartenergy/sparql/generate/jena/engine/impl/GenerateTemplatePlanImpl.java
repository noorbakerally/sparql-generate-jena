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
package com.github.thesmartenergy.sparql.generate.jena.engine.impl;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerateException;
import com.github.thesmartenergy.sparql.generate.jena.engine.GeneratePlan;
import com.github.thesmartenergy.sparql.generate.jena.engine.GenerateTemplateElementPlan;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.log4j.Logger;

/**
 * The GENERATE {...} template.
 *
 * @author maxime.lefrancois
 */
public class GenerateTemplatePlanImpl extends PlanBase implements GeneratePlan {

    /**
     * the logger.
     */
    private static final Logger LOG
            = Logger.getLogger(GenerateTemplatePlanImpl.class);

    /**
     * the list of generate blocks.
     */
    private final List<GenerateTemplateElementPlan> templateElementPlans;

    /**
     * Constructs a new Generate plan from the given list of generate block
     * plans.
     *
     * @param templateElementPlans a list of plans
     */
    public GenerateTemplatePlanImpl(
            final List<GenerateTemplateElementPlan> templateElementPlans) {
        this.templateElementPlans = templateElementPlans;
    }

    /**
     * gets the list of plans for generate blocks.
     *
     * @return a list of plans
     */
    public List<GenerateTemplateElementPlan> getTemplateElementsPlans() {
        return templateElementPlans;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void exec(
            final Dataset inputDataset,
            final Model initialModel,
            final List<Var> variables,
            final List<BindingHashMapOverwrite> values) {
        LOG.debug("exec");
        //TODO write unit tests for bNodes
        for (BindingHashMapOverwrite binding : values) {
            // create new blank node map
            Map<Node, Node> bNodeMap = new HashMap<>();
            for (Var v : binding.varsList()) {
                Node n = binding.get(v);
                if (n.isBlank()) {
                    Node bn = NodeFactory.createBlankNode();
                    bNodeMap.put(n, bn);
                }
            }
            for (GenerateTemplateElementPlan el : templateElementPlans) {
                if (el instanceof GenerateTriplesPlanImpl) {
                    GenerateTriplesPlanImpl subPlanTriples
                            = (GenerateTriplesPlanImpl) el;
                    subPlanTriples.exec(
                            inputDataset, initialModel,
                            binding, bNodeMap);
                } else if (el instanceof RootPlanImpl) {
                    RootPlanImpl rootPlan = (RootPlanImpl) el;
                    QuerySolutionMap b = new QuerySolutionMap();
                    for (Var v : binding.varsList()) {
                        Node n = binding.get(v);
                        if (bNodeMap.containsKey(n)) {
                            b.add(v.getVarName(), initialModel.asRDFNode(bNodeMap.get(n)));
                        } else {
                            b.add(v.getVarName(), initialModel.asRDFNode(n));
                        }
                    }
                    rootPlan.exec(inputDataset, b, initialModel);
                } else {
                    throw new SPARQLGenerateException("should not reach this point");
                }
            }
        }
    }

}
