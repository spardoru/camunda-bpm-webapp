/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.impl.db;

import java.util.List;

import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.QueryMaxResultsLimitUtil;

public class QueryServiceImpl implements QueryService {

  private CommandExecutor commandExecutor;

  public QueryServiceImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public <T> List<T> executeQuery(final String statement, final QueryParameters<T> parameter) {
    List<T> queryResult = commandExecutor.executeCommand(new Command<List<T>>() {

      @SuppressWarnings("unchecked")
      public List<T> execute(CommandContext commandContext) {
        commandContext.getAuthorizationManager()
          .enableQueryAuthCheck(parameter.getAuthCheck());

        if (parameter.isMaxResultsLimitEnabled()) {
          QueryMaxResultsLimitUtil.checkMaxResultsLimit(parameter.getMaxResults(),
            getProcessEngineConfiguration(commandContext));
        }

        return (List<T>) commandContext.getDbSqlSession().selectList(statement, parameter);
      }

    });
    return queryResult;
  }

  public <T> T executeQuery(final String statement, final Object parameter, final Class<T> clazz) {
      T queryResult = commandExecutor.executeCommand(new Command<T>() {

      @SuppressWarnings("unchecked")
      public T execute(CommandContext commandContext) {
        return (T) commandContext.getDbSqlSession().selectOne(statement, parameter);
      }

    });
    return queryResult;
  }

  public Long executeQueryRowCount(final String statement, final ListQueryParameterObject parameter) {
    Long queryResult = commandExecutor.executeCommand(new Command<Long>() {

      public Long execute(CommandContext commandContext) {
        commandContext.getAuthorizationManager()
          .enableQueryAuthCheck(parameter.getAuthCheck());
        return (Long) commandContext.getDbSqlSession().selectOne(statement, parameter);
      }

    });
    return queryResult;
  }

  protected ProcessEngineConfigurationImpl getProcessEngineConfiguration(
    CommandContext commandContext) {
    QuerySessionFactory querySessionFactory =
      (QuerySessionFactory) commandContext.getProcessEngineConfiguration();

    ProcessEngineConfigurationImpl processEngineConfiguration = null;
    if (querySessionFactory != null) {
      processEngineConfiguration = querySessionFactory.getWrappedConfiguration();
    }

    if (processEngineConfiguration == null) {
      throw new ProcessEngineException("Process Engine Configuration missing!");
    }

    return processEngineConfiguration;
  }

}
