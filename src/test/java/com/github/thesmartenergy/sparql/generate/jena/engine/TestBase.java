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
package com.github.thesmartenergy.sparql.generate.jena.engine;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerateException;
import com.github.thesmartenergy.sparql.generate.jena.query.SPARQLGenerateQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.util.Locator;
import org.apache.jena.util.LocatorFile;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author maxime.lefrancois
 */
public class TestBase {

    static Logger LOG;
    static URL examplePath;
    static File exampleDir;
    static FileManager fileManager;

    static void setUpClass(Class clazz) throws Exception {
        LOG = Logger.getLogger(clazz);
        LOG.debug(clazz.getName());
        String dir = clazz.getSimpleName();
        dir = Character.toLowerCase(dir.charAt(0))
                + (dir.length() > 1 ? dir.substring(1) : "");
        examplePath = clazz.getResource("/" + dir);
       
        exampleDir = new File(examplePath.toURI());

        // read location-mapping
        URI confUri = exampleDir.toURI().resolve("configuration.ttl");
        Model conf = RDFDataMgr.loadModel(confUri.toString());

        // initialize file manager
        fileManager = FileManager.makeGlobal();
        Locator loc = new LocatorFile(exampleDir.toURI().getPath());
        LocationMapper mapper = new LocationMapper(conf);
        fileManager.addLocator(loc);
        fileManager.setLocationMapper(mapper);
    }

    void testQuerySerialization() throws Exception {
        String qstring = IOUtils.toString(fileManager.open("query.rqg"), "UTF-8");
        SPARQLGenerateQuery q = (SPARQLGenerateQuery) QueryFactory.create(qstring, SPARQLGenerate.SYNTAX);
        LOG.debug(qstring);

        // serialize query 
        URI queryOutputUri = exampleDir.toURI().resolve("query_serialized.rqg");
        File queryOutputFile = new File(queryOutputUri);
        try (OutputStream queryOutputStream = new FileOutputStream(queryOutputFile)) {
            queryOutputStream.write(q.toString().getBytes());
        }
        LOG.debug(q);

        SPARQLGenerateQuery q2 = (SPARQLGenerateQuery) QueryFactory.create(q.toString(), SPARQLGenerate.SYNTAX);
//        assertTrue(q.equals(q2));
    }

    void testPlanExecution() throws Exception {
        String query = IOUtils.toString(fileManager.open("query.rqg"), "UTF-8");
        SPARQLGenerateQuery q = (SPARQLGenerateQuery) QueryFactory.create(query, SPARQLGenerate.SYNTAX);

        // create generation plan
        PlanFactory factory = new PlanFactory(fileManager);
        RootPlan plan = factory.create(q);
        Model output = ModelFactory.createDefaultModel();
        QuerySolutionMap initialBinding = null;

        // execute plan
        plan.exec(initialBinding, output);

        // write output
       
        String fileName = exampleDir.toString()+"/output.ttl";
        FileWriter out = new FileWriter( fileName );
        try {
            output.write( out, "TTL" );
        }
        finally {
           try {
               out.close();
           }
           catch (IOException closeException) {
              LOG.debug("Error while writing to file");
           }
        }

        URI expectedOutputUri = exampleDir.toURI().resolve("expected_output.ttl");
        Model expectedOutput = RDFDataMgr.loadModel(expectedOutputUri.toString());
        StringWriter sw = new StringWriter();
        LOG.debug(expectedOutput.write(sw, "TTL"));

        //assertTrue(output.isIsomorphicWith(expectedOutput));
       
    }
}
