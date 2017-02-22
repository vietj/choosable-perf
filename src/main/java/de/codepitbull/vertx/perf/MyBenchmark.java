/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.codepitbull.vertx.perf;

import de.codepitbull.vertx.perf.baseline.SimpleChoosable;
import de.codepitbull.vertx.perf.hazelcast.ChoosableSet;
import de.codepitbull.vertx.perf.others.CASChoosable;
import de.codepitbull.vertx.perf.others.CASOptChoosable;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.spi.cluster.jgroups.impl.domain.ChoosableArrayListImpl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Threads(4)
@Fork(value = 1, jvmArgs = { "-XX:+UseBiasedLocking", "-XX:BiasedLockingStartupDelay=0", "-XX:+AggressiveOpts"})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class MyBenchmark {

    @Param({"1", "10", "100"})
    public int size;

    ChoosableIterable<String> baselineChoosable;
    ChoosableIterable<String> hazelcastChoosable;
    ChoosableIterable<String> jgroupsChoosable;
    ChoosableIterable<String> casChoosable;
    ChoosableIterable<String> casBackoffChoosable;

    @Setup
    public void setup(Blackhole blackhole) {
        List<String> list = new ArrayList<>();
        ChoosableSet<String> hazelcast = new ChoosableSet<>(10);
        ChoosableArrayListImpl<String> jgroups = new ChoosableArrayListImpl<>();
        for (int i = 0;i < size;i++) {
            String elt = "element-" + i;
            hazelcast.add(elt);
            jgroups.add(elt);
            list.add(elt);
        }
        this.hazelcastChoosable = hazelcast;
        this.jgroupsChoosable = jgroups;
        this.baselineChoosable = new SimpleChoosable<>("element");
        this.casChoosable = new CASChoosable<>(list);
        this.casBackoffChoosable = new CASOptChoosable<>(list);
    }

    @Benchmark
    public void baselineChoosable(Blackhole blackhole) {
        blackhole.consume(baselineChoosable.choose());
    }

    @Benchmark
    public void hazelcastChoosable(Blackhole blackhole) {
        blackhole.consume(hazelcastChoosable.choose());
    }

    @Benchmark
    public void jgroupsChoosable(Blackhole blackhole) {
        blackhole.consume(jgroupsChoosable.choose());
    }

    @Benchmark
    public void casChoosable(Blackhole blackhole) {
        blackhole.consume(casChoosable.choose());
    }

    @Benchmark
    public void casBackoffChoosable(Blackhole blackhole) {
        blackhole.consume(casBackoffChoosable.choose());
    }
}
