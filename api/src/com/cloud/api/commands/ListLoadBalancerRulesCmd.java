// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.LoadBalancerResponse;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.utils.Pair;

@Implementation(description = "Lists load balancer rules.", responseObject = LoadBalancerResponse.class)
public class ListLoadBalancerRulesCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = Logger.getLogger(ListLoadBalancerRulesCmd.class.getName());

    private static final String s_name = "listloadbalancerrulesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @IdentityMapper(entityTableName="firewall_rules")
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the ID of the load balancer rule")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the load balancer rule")
    private String loadBalancerRuleName;

    @IdentityMapper(entityTableName="user_ip_address")
    @Parameter(name = ApiConstants.PUBLIC_IP_ID, type = CommandType.LONG, description = "the public IP address id of the load balancer rule	")
    private Long publicIpId;

    @IdentityMapper(entityTableName="vm_instance")
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.LONG, description = "the ID of the virtual machine of the load balancer rule")
    private Long virtualMachineId;

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.LONG, description = "the availability zone ID")
    private Long zoneId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getLoadBalancerRuleName() {
        return loadBalancerRuleName;
    }

    public Long getPublicIpId() {
        return publicIpId;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public void execute() {
        Pair<List<? extends LoadBalancer>, Integer> loadBalancers = _lbService.searchForLoadBalancers(this);
        ListResponse<LoadBalancerResponse> response = new ListResponse<LoadBalancerResponse>();
        List<LoadBalancerResponse> lbResponses = new ArrayList<LoadBalancerResponse>();
        if (loadBalancers != null) {
            for (LoadBalancer loadBalancer : loadBalancers.first()) {
                LoadBalancerResponse lbResponse = _responseGenerator.createLoadBalancerResponse(loadBalancer);
                lbResponse.setObjectName("loadbalancerrule");
                lbResponses.add(lbResponse);
            }
        }
        response.setResponses(lbResponses, loadBalancers.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}