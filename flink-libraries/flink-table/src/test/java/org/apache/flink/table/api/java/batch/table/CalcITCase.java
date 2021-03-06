/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.api.java.batch.table;

import java.util.Arrays;
import java.util.Collection;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.types.Row;
import org.apache.flink.table.api.scala.batch.utils.TableProgramsTestBase;
import org.apache.flink.table.api.Table;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.test.javaApiOperators.util.CollectionDataSets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class CalcITCase extends TableProgramsTestBase {

	public CalcITCase(TestExecutionMode mode, TableConfigMode configMode){
		super(mode, configMode);
	}

	@Parameterized.Parameters(name = "Execution mode = {0}, Table config = {1}")
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
			{ TestExecutionMode.COLLECTION, TableProgramsTestBase.DEFAULT() },
			{ TestExecutionMode.COLLECTION, TableProgramsTestBase.NO_NULL() }
		});
	}

	@Test
	public void testSimpleSelectAllWithAs() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> ds = CollectionDataSets.get3TupleDataSet(env);
		Table in = tableEnv.fromDataSet(ds, "a,b,c");

		Table result = in
				.select("a, b, c");

		DataSet<Row> resultSet = tableEnv.toDataSet(result, Row.class);
		List<Row> results = resultSet.collect();
		String expected = "1,1,Hi\n" + "2,2,Hello\n" + "3,2,Hello world\n" +
			"4,3,Hello world, how are you?\n" + "5,3,I am fine.\n" + "6,3,Luke Skywalker\n" +
			"7,4,Comment#1\n" + "8,4,Comment#2\n" + "9,4,Comment#3\n" + "10,4,Comment#4\n" +
			"11,5,Comment#5\n" + "12,5,Comment#6\n" + "13,5,Comment#7\n" +
			"14,5,Comment#8\n" + "15,5,Comment#9\n" + "16,6,Comment#10\n" +
			"17,6,Comment#11\n" + "18,6,Comment#12\n" + "19,6,Comment#13\n" +
			"20,6,Comment#14\n" + "21,6,Comment#15\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testSimpleSelectWithNaming() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> ds = CollectionDataSets.get3TupleDataSet(env);
		Table in = tableEnv.fromDataSet(ds);

		Table result = in
				.select("f0 as a, f1 as b")
				.select("a, b");

		DataSet<Row> resultSet = tableEnv.toDataSet(result, Row.class);
		List<Row> results = resultSet.collect();
		String expected = "1,1\n" + "2,2\n" + "3,2\n" + "4,3\n" + "5,3\n" + "6,3\n" + "7,4\n" +
				"8,4\n" + "9,4\n" + "10,4\n" + "11,5\n" + "12,5\n" + "13,5\n" + "14,5\n" + "15,5\n" +
				"16,6\n" + "17,6\n" + "18,6\n" + "19,6\n" + "20,6\n" + "21,6\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testSimpleSelectRenameAll() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> ds = CollectionDataSets.get3TupleDataSet(env);
		Table in = tableEnv.fromDataSet(ds);

		Table result = in
			.select("f0 as a, f1 as b, f2 as c")
			.select("a, b");

		DataSet<Row> resultSet = tableEnv.toDataSet(result, Row.class);
		List<Row> results = resultSet.collect();
		String expected = "1,1\n" + "2,2\n" + "3,2\n" + "4,3\n" + "5,3\n" + "6,3\n" + "7,4\n" +
			"8,4\n" + "9,4\n" + "10,4\n" + "11,5\n" + "12,5\n" + "13,5\n" + "14,5\n" + "15,5\n" +
			"16,6\n" + "17,6\n" + "18,6\n" + "19,6\n" + "20,6\n" + "21,6\n";
		compareResultAsText(results, expected);
	}

	@Test(expected = ValidationException.class)
	public void testSelectInvalidField() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> ds = CollectionDataSets.get3TupleDataSet(env);

		tableEnv.fromDataSet(ds, "a, b, c")
			// Must fail. Field foo does not exist
			.select("a + 1, foo + 2");
	}

	@Test(expected = ValidationException.class)
	public void testSelectAmbiguousFieldNames() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> ds = CollectionDataSets.get3TupleDataSet(env);

		tableEnv.fromDataSet(ds, "a, b, c")
			// Must fail. Field foo does not exist
			.select("a + 1 as foo, b + 2 as foo");
	}

	@Test
	public void testSelectStar() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> ds = CollectionDataSets.get3TupleDataSet(env);
		Table in = tableEnv.fromDataSet(ds, "a,b,c");

		Table result = in
			.select("*");

		DataSet<Row> resultSet = tableEnv.toDataSet(result, Row.class);
		List<Row> results = resultSet.collect();
		String expected = "1,1,Hi\n" + "2,2,Hello\n" + "3,2,Hello world\n" +
		                  "4,3,Hello world, how are you?\n" + "5,3,I am fine.\n" + "6,3,Luke Skywalker\n" +
		                  "7,4,Comment#1\n" + "8,4,Comment#2\n" + "9,4,Comment#3\n" + "10,4,Comment#4\n" +
		                  "11,5,Comment#5\n" + "12,5,Comment#6\n" + "13,5,Comment#7\n" +
		                  "14,5,Comment#8\n" + "15,5,Comment#9\n" + "16,6,Comment#10\n" +
		                  "17,6,Comment#11\n" + "18,6,Comment#12\n" + "19,6,Comment#13\n" +
		                  "20,6,Comment#14\n" + "21,6,Comment#15\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testAllRejectingFilter() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = CollectionDataSets.get3TupleDataSet(env);
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		Table result = table
				.filter("false");

		DataSet<Row> ds = tableEnv.toDataSet(result, Row.class);
		List<Row> results = ds.collect();
		String expected = "\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testAllPassingFilter() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = CollectionDataSets.get3TupleDataSet(env);
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		Table result = table
				.filter("true");

		DataSet<Row> ds = tableEnv.toDataSet(result, Row.class);
		List<Row> results = ds.collect();
		String expected = "1,1,Hi\n" + "2,2,Hello\n" + "3,2,Hello world\n" +
			"4,3,Hello world, how are you?\n" + "5,3,I am fine.\n" + "6,3,Luke Skywalker\n" +
			"7,4,Comment#1\n" + "8,4,Comment#2\n" + "9,4,Comment#3\n" + "10,4,Comment#4\n" +
			"11,5,Comment#5\n" + "12,5,Comment#6\n" + "13,5,Comment#7\n" +
			"14,5,Comment#8\n" + "15,5,Comment#9\n" + "16,6,Comment#10\n" +
			"17,6,Comment#11\n" + "18,6,Comment#12\n" + "19,6,Comment#13\n" +
			"20,6,Comment#14\n" + "21,6,Comment#15\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testFilterOnIntegerTupleField() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = CollectionDataSets.get3TupleDataSet(env);
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		Table result = table
				.filter(" a % 2 = 0 ");

		DataSet<Row> ds = tableEnv.toDataSet(result, Row.class);
		List<Row> results = ds.collect();
		String expected = "2,2,Hello\n" + "4,3,Hello world, how are you?\n" + "6,3,Luke Skywalker\n" + "8,4," +
				"Comment#2\n" + "10,4,Comment#4\n" + "12,5,Comment#6\n" + "14,5,Comment#8\n" + "16,6," +
				"Comment#10\n" + "18,6,Comment#12\n" + "20,6,Comment#14\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testNotEquals() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = CollectionDataSets.get3TupleDataSet(env);
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		Table result = table
				.filter("!( a % 2 <> 0 ) ");

		DataSet<Row> ds = tableEnv.toDataSet(result, Row.class);
		List<Row> results = ds.collect();
		String expected = "2,2,Hello\n" + "4,3,Hello world, how are you?\n" + "6,3,Luke Skywalker\n" + "8,4," +
				"Comment#2\n" + "10,4,Comment#4\n" + "12,5,Comment#6\n" + "14,5,Comment#8\n" + "16,6," +
				"Comment#10\n" + "18,6,Comment#12\n" + "20,6,Comment#14\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testDisjunctivePreds() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = CollectionDataSets.get3TupleDataSet(env);
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		Table result = table
			.filter("a < 2 || a > 20");

		DataSet<Row> ds = tableEnv.toDataSet(result, Row.class);
		List<Row> results = ds.collect();
		String expected = "1,1,Hi\n" + "21,6,Comment#15\n";
		compareResultAsText(results, expected);
	}

	@Test
	public void testIntegerBiggerThan128() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = env.fromElements(new Tuple3<>(300, 1L, "Hello"));
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		Table result = table
			.filter("a = 300 ");

		DataSet<Row> ds = tableEnv.toDataSet(result, Row.class);
		List<Row> results = ds.collect();
		String expected = "300,1,Hello\n";
		compareResultAsText(results, expected);
	}

	@Test(expected = ValidationException.class)
	public void testFilterInvalidField() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		DataSet<Tuple3<Integer, Long, String>> input = CollectionDataSets.get3TupleDataSet(env);
		Table table = tableEnv.fromDataSet(input, "a, b, c");

		table
			// Must fail. Field foo does not exist.
			.filter("foo = 17");
	}

	public static class OldHashCode extends ScalarFunction {
		public int eval(String s) {
			return -1;
		}
	}

	public static class HashCode extends ScalarFunction {
		public int eval(String s) {
			return s.hashCode();
		}
	}

	@Test
	public void testUserDefinedScalarFunction() throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env, config());

		tableEnv.registerFunction("hashCode", new OldHashCode());
		tableEnv.registerFunction("hashCode", new HashCode());

		DataSource<String> input = env.fromElements("a", "b", "c");

		Table table = tableEnv.fromDataSet(input, "text");

		Table result = table.select("text.hashCode()");

		DataSet<Integer> ds = tableEnv.toDataSet(result, Integer.class);
		List<Integer> results = ds.collect();
		String expected = "97\n98\n99";
		compareResultAsText(results, expected);
	}

}

