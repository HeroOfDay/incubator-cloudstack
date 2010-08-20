/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.vm;

import java.util.HashMap;
import java.util.List;

import com.cloud.agent.api.VmStatsEntry;
import com.cloud.api.ServerApiException;
import com.cloud.api.commands.RecoverVMCmd;
import com.cloud.api.commands.ResetVMPasswordCmd;
import com.cloud.api.commands.StartVMCmd;
import com.cloud.api.commands.StopVMCmd;
import com.cloud.api.commands.UpdateVMCmd;
import com.cloud.api.commands.UpgradeVMCmd;
import com.cloud.async.executor.DestroyVMExecutor;
import com.cloud.async.executor.OperationResponse;
import com.cloud.async.executor.RebootVMExecutor;
import com.cloud.async.executor.StartVMExecutor;
import com.cloud.async.executor.StopVMExecutor;
import com.cloud.async.executor.VMOperationParam;
import com.cloud.dc.DataCenterVO;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientStorageCapacityException;
import com.cloud.exception.InternalErrorException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.network.security.NetworkGroupVO;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.AccountVO;
import com.cloud.uservm.UserVm;
import com.cloud.utils.component.Manager;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.vm.VirtualMachine.Event;

/**
 *
 * UserVmManager contains all of the code to work with user VMs.
 * 
 */
public interface UserVmManager extends Manager, VirtualMachineManager<UserVmVO> {

    UserVmVO allocate(String displayName, VMTemplateVO template, ServiceOfferingVO serviceOffering, NetworkOfferingVO[] networkOfferings, DiskOfferingVO[] diskOfferings, AccountVO owner, long userId) throws InsufficientCapacityException;
    
	static final int MAX_USER_DATA_LENGTH_BYTES = 2048;
    /**
     * @param hostId get all of the virtual machines that belong to one host.
     * @return collection of VirtualMachine.
     */
    List<? extends UserVm> getVirtualMachines(long hostId);
    
    /**
     * @param vmId id of the virtual machine.
     * @return VirtualMachine
     */
    UserVmVO getVirtualMachine(long vmId);
    
    /**
     * creates a virtual machine.
     * @param userId the id of the user performing the action
     * @param account account creating the virtual machine.
     * @param dc data center to deploy it in.
     * @param offering the service offering that comes with it.
     * @param template template to base the virtual machine on. Can either represent an ISO, or a normal template.
     * @param diskOffering the disk offering for the root disk (deploying from ISO) or the data disk (deploying from a normal template)
     * @return UserVmVO if created; null if not.
     */
    UserVmVO createVirtualMachine(Long vmId, long userId, AccountVO account, DataCenterVO dc, ServiceOfferingVO offering, VMTemplateVO template, DiskOfferingVO diskOffering, String displayName, String group, String userData, List<StoragePoolVO> avoids, long startEventId, long size) throws InsufficientStorageCapacityException, InternalErrorException, ResourceAllocationException;
    
	UserVmVO createDirectlyAttachedVM(Long vmId, long userId, AccountVO account, DataCenterVO dc, ServiceOfferingVO offering, VMTemplateVO template, DiskOfferingVO diskOffering, String displayName, String group, String userData, List<StoragePoolVO> a, List<NetworkGroupVO> networkGroupVO, long startEventId, long size) throws InternalErrorException, ResourceAllocationException;

	UserVmVO createDirectlyAttachedVMExternal(Long vmId, long userId, AccountVO account, DataCenterVO dc, ServiceOfferingVO offering, VMTemplateVO template, DiskOfferingVO diskOffering, String displayName, String group, String userData, List<StoragePoolVO> a, List<NetworkGroupVO> networkGroupVO, long startEventId, long size) throws InternalErrorException, ResourceAllocationException;

    /**
     * Destroys one virtual machine
     * @param userId the id of the user performing the action
     * @param vmId the id of the virtual machine.
     */
    boolean destroyVirtualMachine(long userId, long vmId);
    OperationResponse executeDestroyVM(DestroyVMExecutor executor, VMOperationParam param);
    
    
    /**
     * Resets the password of a virtual machine.
     * @param userId the id of the user performing the action
     * @param vmId the id of the virtual machine.
     * @param password the password of the virtual machine.
     * @param true if reset worked successfully, false otherwise
     */
    boolean resetVMPassword(ResetVMPasswordCmd cmd);
    
    /**
     * Attaches the specified volume to the specified VM
     * @param vmId
     * @param volumeId
     * @throws InternalErrorException
     */
    void attachVolumeToVM(long vmId, long volumeId, Long deviceId, long startEventId) throws InternalErrorException;
    
    /**
     * Detaches the specified volume from the VM it is currently attached to.
     * @param volumeId
     * @throws InternalErrorException
     */
    void detachVolumeFromVM(long volumeId, long startEventId) throws InternalErrorException;
    
    /**
     * Attaches an ISO to the virtual CDROM device of the specified VM. Will eject any existing virtual CDROM if isoPath is null.
     * @param vmId
     * @param isoId
     * @param attach whether to attach or detach the given iso
     * @return
     */
    boolean attachISOToVM(long vmId, long isoId, boolean attach);
    
    /**
     * Start the virtual machine.
     * @param userId the id of the user performing the action
     * @param vmId the id of the virtual machine.
     * @param isoPath path of the ISO from which the VM should be booted (optional)
     * @throws ExecutionException 
     * @throws StorageUnavailableException 
     * @throws ConcurrentOperationException 
     */
    UserVmVO startVirtualMachine(long userId, long vmId, String isoPath, long startEventId) throws ExecutionException, StorageUnavailableException, ConcurrentOperationException;
    boolean executeStartVM(StartVMExecutor executor, VMOperationParam param);
    
    /**
     * Start the virtual machine.
     * @param userId the id of the user performing the action
     * @param vmId the id of the virtual machine.
     * @param password the password that the user wants to use to access the virtual machine
     * @param isoPath path of the ISO from which the VM should be booted (optional)
     * @throws ExecutionException 
     * @throws StorageUnavailableException 
     * @throws ConcurrentOperationException 
     */
    UserVmVO startVirtualMachine(long userId, long vmId, String password, String isoPath, long startEventId) throws ExecutionException, StorageUnavailableException, ConcurrentOperationException;
    UserVmVO startVirtualMachine(StartVMCmd cmd) throws StorageUnavailableException, ExecutionException, ConcurrentOperationException;
    
    /**
     * Stops the virtual machine
     * @param userId the id of the user performing the action
     * @param vmId
     * @param eventId -- id of the scheduled event for stopping vm
     * @return true if stopped; false if problems.
     */
    boolean stopVirtualMachine(long userId, long vmId, long eventId);
    boolean stopVirtualMachine(StopVMCmd cmd) throws ServerApiException;
    OperationResponse executeStopVM(StopVMExecutor executor, VMOperationParam param);
    void completeStopCommand(long userId, UserVmVO vm, Event e, long startEventId);
    

    /**
     * upgrade the service offering of the virtual machine
     * @param cmd
     * @return success/failure
     * @throws InvalidParameterValueException 
     */
    boolean upgradeVirtualMachine(UpgradeVMCmd cmd) throws ServerApiException, InvalidParameterValueException;
    
    /**
     * Obtains statistics for a list of host or VMs; CPU and network utilization
     * @param host ID
     * @param host name
     * @param list of VM IDs or host id
     * @return GetVmStatsAnswer
     * @throws InternalErrorException
     */
    HashMap<Long, VmStatsEntry> getVirtualMachineStatistics(long hostId, String hostName, List<Long> vmIds) throws InternalErrorException;
    
    boolean rebootVirtualMachine(long userId, long vmId);
    OperationResponse executeRebootVM(RebootVMExecutor executor, VMOperationParam param);
    
    boolean recoverVirtualMachine(RecoverVMCmd cmd) throws ResourceAllocationException, InternalErrorException;

    VMTemplateVO createPrivateTemplateRecord(Long userId, long vmId, String name, String description, long guestOsId, Boolean requiresHvm, Integer bits, Boolean passwordEnabled, boolean isPublic, boolean featured)
		throws InvalidParameterValueException;
    
    /**
     * Creates a private template from a snapshot of a VM
     * @param template the template record that is used to store data (we need instance be created first)
     * @param snapshotId the id of the snaphot to use for the template
     * @param name the user given name of the private template
     * @param description the user give description (aka display text) for the template
     * @return a template if successfully created, null otherwise
     */
    VMTemplateVO createPrivateTemplate(VMTemplateVO template, Long userId, long snapshotId, String name, String description);

    /**
     * @param userId    The Id of the user who invoked this operation.
     * @param volumeId  The volume for which this snapshot is being taken
     * @return          The properties of the snapshot taken
     */
    SnapshotVO createTemplateSnapshot(long userId, long volumeId);
    boolean destroyTemplateSnapshot(Long userId, long snapshotId);

    /**
     * Clean the network rules for the given VM
     * @param userId
     * @param instanceId the id of the instance for which the network rules should be cleaned
     */
    void cleanNetworkRules(long userId, long instanceId);
    
    /**
     * Releases a guest IP address for a VM. If the VM is on a direct attached network, will also unassign the IP address.
     * @param userVm
     */
    void releaseGuestIpAddress(UserVmVO userVm);

	void updateVirtualMachine(UpdateVMCmd cmd);

}
