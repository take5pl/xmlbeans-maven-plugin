package org.codehaus.mojo.xmlbeans;

/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.codehaus.plexus.util.FileUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @version $Rev: 46019 $ $Date$
 */
public class SchemaCompilerWrapper
{

    public static void compileSchemas( File baseDirectory, String sourceSchemas, String xmlConfigs, String targetDir,
                                       String catalogLocation, List classpath )
        throws Exception
    {
        List schemas = FileUtils.getFiles( baseDirectory, sourceSchemas, null );

        List configs = FileUtils.getFiles( baseDirectory, xmlConfigs, null );

        EntityResolver entityResolver = null;
        if ( catalogLocation != null )
        {
            CatalogManager catalogManager = CatalogManager.getStaticManager();
            catalogManager.setCatalogFiles( catalogLocation );
            entityResolver = new PassThroughResolver( new CatalogResolver() );
        }

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setBaseDir( null );
        params.setXsdFiles( (File[]) schemas.toArray( new File[]{} ) );
        params.setWsdlFiles( new File[]{} );
        params.setJavaFiles( new File[]{} );
        params.setConfigFiles( (File[]) configs.toArray( new File[]{} ) );
        params.setClasspath( (File[]) classpath.toArray( new File[]{} ) );
        params.setOutputJar( null );
        params.setName( null );
        params.setSrcDir( new File( targetDir ) );
        params.setClassesDir( new File( targetDir ) );
        params.setCompiler( null );
        params.setJar( null );
        params.setMemoryInitialSize( null );
        params.setMemoryMaximumSize( null );
        params.setNojavac( true );
        params.setQuiet( false );
        params.setVerbose( true );
        params.setDownload( false );
        params.setNoUpa( false );
        params.setNoPvr( false );
        params.setDebug( true );
        params.setErrorListener( new ArrayList() );
        params.setRepackage( null );
        params.setExtensions( null );
        params.setJaxb( false );
        params.setMdefNamespaces( null );
        params.setEntityResolver( entityResolver );

        boolean result = SchemaCompiler.compile( params );
        if ( !result )
        {
            Collection errors = params.getErrorListener();
            for ( Iterator iterator = errors.iterator(); iterator.hasNext(); )
            {
                Object o = (Object) iterator.next();
                System.out.println( "xmlbeans error: " + o );
            }
            throw new Exception( "Schema compilation failed" );
        }

    }

    private static class PassThroughResolver
        implements EntityResolver
    {

        private final EntityResolver delegate;

        public PassThroughResolver( EntityResolver delegate )
        {
            this.delegate = delegate;
        }

        public InputSource resolveEntity( String publicId, String systemId )
            throws SAXException, IOException
        {
            InputSource is = delegate.resolveEntity( publicId, systemId );
            if ( is != null )
            {
                return is;
            }
            System.out.println(
                "Could not resolve publicId: " + publicId + ", systemId: " + systemId + " from catalog" );
            return new InputSource( systemId );
        }

    }
}
