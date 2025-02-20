// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.tunnel;

import edu.rice.cs.hpcremote.IRemoteCommunicationProtocol;
import edu.rice.cs.hpcremote.data.IRemoteDirectoryBrowser;

/***
 * General interface to manage communication for a specific remote host
 */
public interface IRemoteCommunication extends IRemoteDirectoryBrowser, IRemoteCommunicationProtocol 
{
}
