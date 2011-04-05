/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.testsuite.integration.deployment.classloading.ear;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the extended-class-visibility option in jboss-structure.xml
 *
 * By default ejb-jar's should not be visible to each other, with this option enabled they are.
 *
 */
@RunWith(Arquillian.class)
public class EarJbossStructureExtendedVisibilityTestCase {

    @Deployment
    public static Archive<?> deploy() {

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class);

        WebArchive war = ShrinkWrap.create(WebArchive.class,"test.war");
        war.addClasses(TestAA.class);
        ear.addModule(war);

        JavaArchive ejb = ShrinkWrap.create(JavaArchive.class, "ejb1.jar");
        ejb.addClasses(MyEjb.class, EarJbossStructureExtendedVisibilityTestCase.class);
        ear.addModule(ejb);

        ejb = ShrinkWrap.create(JavaArchive.class, "ejb2.jar");
        ejb.addClasses(MyEjb2.class);
        ear.addModule(ejb);

        ear.addManifestResource(new StringAsset(
               "<jboss-deployment-structure><extended-class-visibility>true</extended-class-visibility></jboss-deployment-structure>"),
                "jboss-deployment-structure.xml");

        return ear;
    }

    @Test(expected = ClassNotFoundException.class)
    public void testWarModuleStillNotAccessible() throws ClassNotFoundException {
        loadClass("org.jboss.as.testsuite.integration.deployment.classloading.ear.TestAA",getClass().getClassLoader());
    }

    @Test
    public void testOtherEjbJarAcessible() throws ClassNotFoundException {
        loadClass("org.jboss.as.testsuite.integration.deployment.classloading.ear.MyEjb2",getClass().getClassLoader());
    }


    private static Class<?> loadClass(String name, ClassLoader cl) throws ClassNotFoundException {
        if (cl != null) {
            return Class.forName(name, false, cl);
        } else
            return Class.forName(name);
    }
}
