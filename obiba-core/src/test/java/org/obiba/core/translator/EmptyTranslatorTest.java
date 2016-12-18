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
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

public class EmptyTranslatorTest {

    @Test
    public void always_returns_key_passed_as_param() throws Exception {

        EmptyTranslator translator = new EmptyTranslator();

        assertThat(translator.translate(null), is(nullValue()));
        assertThat(translator.translate(""), is(""));
        assertThat(translator.translate("test"), is("test"));
        assertThat(translator.translate("long test with a lot of params"), is("long test with a lot of params"));
    }
}
