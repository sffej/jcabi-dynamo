/**
 * Copyright (c) 2012-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.dynamo.mock;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link H2Data}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 */
public final class H2DataTest {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * H2Data can store and fetch.
     * @throws Exception If some problem inside
     */
    @Test
    public void storesAndReadsAttributes() throws Exception {
        final String table = "users";
        final String key = "id";
        final int number = 43;
        final String attr = "desc";
        final String value = "some\n\t\u20ac text";
        final MkData data = new H2Data().with(
            table, new String[] {key}, new String[] {attr}
        );
        data.put(table, new Attributes().with(key, number).with(attr, value));
        MatcherAssert.assertThat(
            data.iterate(
                table, new Conditions().with(key, Conditions.equalTo(number))
            ).iterator().next(),
            Matchers.hasEntry(
                Matchers.equalTo(attr),
                Matchers.equalTo(new AttributeValue(value))
            )
        );
    }

    /**
     * H2Data can store to a file.
     * @throws Exception If some problem inside
     * @see <a href="https://code.google.com/p/h2database/issues/detail?id=447">
     *  Google Code: DB file extension customizability</a>
     */
    @Test
    @Ignore
    public void storesToFile() throws Exception {
        final File file = this.temp.newFile();
        final String table = "tbl";
        final String key = "key1";
        final MkData data = new H2Data(file).with(
            table, new String[] {key}, new String[0]
        );
        data.put(table, new Attributes().with(key, "x2"));
        MatcherAssert.assertThat(file.exists(), Matchers.is(true));
        MatcherAssert.assertThat(file.length(), Matchers.greaterThan(0L));
    }

    /**
     * H2Data can create many tables.
     * @throws Exception If some problem inside
     */
    @Test
    public void createsManyTables() throws Exception {
        new H2Data()
            .with("firsttable", new String[] {"firstid"}, new String[0])
            .with("secondtable", new String[]{"secondid"}, new String[0]);
    }

    /**
     * H2Data can create tables with long names (max length of DynamoDb table
     * name is 255 characters).
     * @throws Exception In case test fails
     */
    @Test
    public void createsTablesWithLongNames() throws Exception {
        new H2Data()
            .with(
                //@checkstyle MagicNumberCheck (1 line)
                Joiner.on("").join(Collections.nCopies(255, "a")),
                new String[]{"key"}, new String[0]
        );
    }

    /**
     * H2Data supports table names with characters illegal to H2.
     * @throws Exception In case test fails
     */
    @Test
    public void supportsTableNamesWithIllegalCharacters() throws Exception {
        new H2Data().with(".-.", new String[]{"pk"}, new String[0]);
    }

    /**
     * H2Data supports column names with characters illegal to H2.
     * @throws Exception In case test fails
     * @todo #28:30min H2Data doesn't support COLUMNS with ".", "-" or digits
     *  but I don't know for sure should it support these symbols or not.
     *  It's needed to be confirmed and test should be uncommented
     *  when H2Data will be supporting mentioned symbols.
     */
    @Test
    @Ignore
    public void supportsColumnNamesWithIllegalCharacters() throws Exception {
        final String key = "0-.col.-0";
        final String table = "test";
        new H2Data().with(
            table, new String[] {key}, new String[0]
        ).put(table, new Attributes().with(key, "value"));
    }

    /**
     * H2Data can delete records.
     * @throws Exception In case test fails
     */
    @Test
    public void deletesRecords() throws Exception {
        final String table = "customers";
        final String field = "name";
        final String man = "Kevin";
        final String woman = "Helen";
        final H2Data data = new H2Data()
            .with(table, new String[]{field}, new String[0]);
        data.put(
            table,
            new Attributes().with(field, man)
        );
        data.put(
            table,
            new Attributes().with(field, woman)
        );
        data.delete(table, new Attributes().with(field, man));
        final List<Attributes> rest = Lists.newArrayList(
            data.iterate(table, new Conditions())
        );
        MatcherAssert.assertThat(
            rest.size(),
            Matchers.equalTo(1)
        );
        MatcherAssert.assertThat(
            rest.get(0).get(field).getS(),
            Matchers.equalTo(woman)
        );
    }

    /**
     * H2Data can update table attributes.
     * @throws Exception In case test fails
     */
    @Test
    public void updatesTableAttributes() throws Exception {
        final String table = "tests";
        final String key = "tid";
        final int number = 43;
        final String attr = "descr";
        final String value = "Dummy\n\t\u20ac text";
        final String updated = "Updated";
        final MkData data = new H2Data().with(
            table, new String[] {key}, attr
        );
        data.put(table, new Attributes().with(key, number).with(attr, value));
        data.update(
            table,
            new Attributes().with(key, number),
            new AttributeUpdates().with(attr, "something else")
        );
        data.update(
            table,
            new Attributes().with(key, number),
            new AttributeUpdates().with(attr, updated)
        );
        final Iterable<Attributes> result = data.iterate(
            table, new Conditions().with(key, Conditions.equalTo(number))
        );
        MatcherAssert.assertThat(
            result.iterator().next(),
            Matchers.hasEntry(
                Matchers.equalTo(attr),
                Matchers.equalTo(new AttributeValue(updated))
            )
        );
        MatcherAssert.assertThat(
            result,
            Matchers.<Attributes>iterableWithSize(1)
        );
    }
}
