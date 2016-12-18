/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.translator;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JsonTranslatorTest {

    @Test
    public void can_translate_root_value() throws Exception {

        // Given
        String translationsAsString = "{'testKey':'testValue'}";
        JsonTranslator jsonTranslator = new JsonTranslator(translationsAsString);

        // Execute
        String translatedResponse = jsonTranslator.translate("testKey");

        // Verify
        assertThat(translatedResponse, is("testValue"));
    }

    @Test
    public void can_translate_sub_value() throws Exception {

        // Given
        String translationsAsString = "{'testObject':{'testKey':'testValue'}}";
        JsonTranslator jsonTranslator = new JsonTranslator(translationsAsString);

        // Execute
        String translatedResponse = jsonTranslator.translate("testObject.testKey");

        // Verify
        assertThat(translatedResponse, is("testValue"));
    }

    @Test
    public void when_translate_non_existent_key__return_key_as_response() throws Exception {

        // Given
        String translationsAsString = "{'testObject':{'testKey':'testValue'}}";
        JsonTranslator jsonTranslator = new JsonTranslator(translationsAsString);

        // Execute
        String translatedResponse = jsonTranslator.translate("nonExistent");

        // Verify
        assertThat(translatedResponse, is("nonExistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_build_object_with_empty_string__throws_exception() throws Exception {
        new JsonTranslator("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_build_object_with_null__throws_exception() throws Exception {
        new JsonTranslator(null);
    }
}
