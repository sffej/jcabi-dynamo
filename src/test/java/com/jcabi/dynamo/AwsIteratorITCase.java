/**
 * Copyright (c) 2012-2015, jcabi.com
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
package com.jcabi.dynamo;

import com.jcabi.aspects.Tv;
import java.util.Iterator;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration case for {@link AwsIterator}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.16.2
 */
public final class AwsIteratorITCase {

    /**
     * AwsIterator can iterate.
     * @throws Exception If some problem inside
     * @todo #56 Integration test fail when adding @NotNull constraint
     *  this should be fixed.
     */
    @Ignore
    @Test
    public void iteratesItems() throws Exception {
        final String name = RandomStringUtils.randomAlphabetic(Tv.EIGHT);
        final Table tbl = new RegionMock().get(name).table(name);
        tbl.put(
            new Attributes()
                .with(RegionMock.HASH, "test")
                .with(RegionMock.RANGE, 1L)
        );
        MatcherAssert.assertThat(
            tbl.frame(),
            Matchers.hasSize(1)
        );
    }

    /**
     * AwsIterator can iterate and delete items.
     * @throws Exception If some problem inside
     * @todo #56 Integration test fail when adding @NotNull constraint
     *  this should be fixed.
     */
    @Ignore
    @Test
    public void iteratesItemsAndDeletes() throws Exception {
        final String name = RandomStringUtils.randomAlphabetic(Tv.EIGHT);
        final Table tbl = new RegionMock().get(name).table(name);
        final Attributes attrs = new Attributes().with(RegionMock.RANGE, 1L);
        for (int idx = 0; idx < Tv.SIX; ++idx) {
            tbl.put(attrs.with(RegionMock.HASH, String.format("i%d", idx)));
        }
        final Iterator<Item> items = tbl.frame().iterator();
        int cnt = 0;
        while (items.hasNext()) {
            items.next();
            items.remove();
            ++cnt;
            if (cnt > Tv.HUNDRED) {
                throw new IllegalStateException("too many items");
            }
        }
        MatcherAssert.assertThat(
            tbl.frame(),
            Matchers.hasSize(0)
        );
    }

}
