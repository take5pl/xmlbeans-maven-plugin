package org.codehaus.mojo.xmlbeans;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractPlugin;
import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @goal java
 * @phase generate-sources
 * @description Creates java beans from the XML schema.
 * @todo improve parameters using lists when plexus is capable of it
 * @parameter name="targetDirectory" type="String" required="true" validator="" expression="#project.build.directory/generated-sources/xmlbeans" description="The output directory."
 * @parameter name="sourceSchemas" type="String" required="true" validator="" expression="" description="The source schema list."
 * @parameter name="xmlConfigs" type="String" required="true" validator="" expression="" description="The XBean configurations."
 * @parameter name="resolverCatalog" type="String" required="true" validator="" expression="" description="The resolver catalog."
 * @parameter name="dependencies" type="String" required="false" validator="" expression="" defaultValue="" description="The dependencies to add to the classpath."
 * @parameter name="project" type="String" required="true" validator="" expression="#project" description="The Maven project."
 */
public class SchemaMojo
    extends AbstractPlugin
{

    public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
        throws Exception
    {

        // TODO: run up2date? or just wait for the mojo guarded execution...

        String sourceSchemas = (String) request.getParameter( "sourceSchemas" );
        String xmlConfigs = (String) request.getParameter( "xmlConfigs" );
        String targetDirectory = (String) request.getParameter( "targetDirectory" );
        String resolverCatalog = (String) request.getParameter( "resolverCatalog" );
        String dependencies = (String) request.getParameter( "dependencies" );
        MavenProject project = (MavenProject) request.getParameter( "project" );

        List artifacts = new ArrayList();
        if ( dependencies != null )
        {
            String[] deps = StringUtils.split( dependencies, "," );
            for ( int i = 0; i < deps.length; i++ )
            {
                String id = deps[i];
                boolean found = false;
                for ( Iterator j = project.getArtifacts().iterator(); j.hasNext() && !found; )
                {
                    Artifact a = (Artifact) j.next();
                    // TODO: account for type?
                    if ( id.equals( a.getGroupId() + ":" + a.getArtifactId() ) )
                    {
                        artifacts.add( a.getFile() );
                        found = true;
                    }
                }
                if ( !found )
                {
                    throw new Exception( "Unable to find dependency in project with id " + id );
                }
            }
        }

        File basedir = project.getFile().getParentFile();
        File resolver = new File( basedir, resolverCatalog );
        SchemaCompilerWrapper.compileSchemas( basedir, sourceSchemas, xmlConfigs, targetDirectory, resolver, artifacts );

        project.addCompileSourceRoot( targetDirectory );
    }
}
