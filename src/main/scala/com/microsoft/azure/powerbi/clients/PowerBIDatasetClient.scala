/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.powerbi.clients

import com.microsoft.azure.powerbi.common._
import com.microsoft.azure.powerbi.exceptions._
import com.microsoft.azure.powerbi.models._
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.ShortTypeHints

object PowerBIDatasetClient {

  def get(authenticationToken: String, groupId: String = null): PowerBIDatasetDetailsList = {

    var getRequestURL: String = null

    if(groupId == null || groupId.trim.isEmpty) {

      getRequestURL = PowerBIURLs.Datasets

    } else {

      getRequestURL = PowerBIURLs.Groups + f"/$groupId/datasets"
    }

    val getRequest: HttpGet = new HttpGet(getRequestURL)

    getRequest.addHeader("Authorization", f"Bearer $authenticationToken")

    val httpClient : CloseableHttpClient = HttpClientUtils.getCustomHttpClient

    var responseContent: String = null
    var statusCode: Int = -1
    var exceptionMessage: String = null

    try {

      val httpResponse = httpClient.execute(getRequest)

      statusCode = httpResponse.getStatusLine.getStatusCode

      val responseEntity = httpResponse.getEntity

      if (responseEntity != null) {

        val inputStream = responseEntity.getContent
        responseContent = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close()
      }
    }
    catch {

      case e: Exception => exceptionMessage = e.getMessage
    }
    finally {

      httpClient.close()
    }

    if(statusCode == 200) {

      implicit val formats = Serialization.formats(
        ShortTypeHints(
          List()
        )
      )

      return read[PowerBIDatasetDetailsList](responseContent)
    }

    throw PowerBIClientException(statusCode, responseContent, exceptionMessage)
  }

  def create(powerBIDataset: PowerBIDataset, retentionPolicy: PowerBIOptions.DatasetRetentionPolicy,
             authenticationToken: String, groupId: String = null): PowerBIDatasetDetails = {

    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List()
      )
    )

    create(new StringEntity(write(powerBIDataset)), retentionPolicy, authenticationToken, groupId)
  }

  def create(powerBIStreamingDataset: PowerBIStreamingDataset,
             retentionPolicy: PowerBIOptions.DatasetRetentionPolicy,
             authenticationToken: String, groupId: String): PowerBIDatasetDetails = {

    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List()
      )
    )

    create(new StringEntity(write(powerBIStreamingDataset)),
      retentionPolicy, authenticationToken, groupId)
  }

  private def create(postRequestEntity: StringEntity,
                     retentionPolicy: PowerBIOptions.DatasetRetentionPolicy,
                     authenticationToken: String, groupId: String): PowerBIDatasetDetails = {

    var postRequestURL: String = null

    if(groupId == null || groupId.trim.isEmpty) {
      postRequestURL = PowerBIURLs.Datasets
    } else {
      postRequestURL = PowerBIURLs.Groups + f"/$groupId/datasets"
    }

    if(retentionPolicy != null) {
      retentionPolicy match {
        case PowerBIOptions.None | PowerBIOptions.basicFIFO =>
          postRequestURL += f"?defaultRetentionPolicy=" + retentionPolicy.toString
      }
    }

    val postRequest: HttpPost = new HttpPost(postRequestURL)

    postRequest.addHeader("Authorization", f"Bearer $authenticationToken")
    postRequest.addHeader("Content-Type", "application/json")

    postRequest.setEntity(postRequestEntity)

    val httpClient : CloseableHttpClient = HttpClientUtils.getCustomHttpClient

    var responseContent: String = null
    var statusCode: Int = -1
    var exceptionMessage: String = null

    try {
      val httpResponse = httpClient.execute(postRequest)
      statusCode = httpResponse.getStatusLine.getStatusCode

      val responseEntity = httpResponse.getEntity

      if (responseEntity != null) {

        val inputStream = responseEntity.getContent
        responseContent = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close()
      }
    }
    catch {
      case e: Exception => exceptionMessage = e.getMessage
    }
    finally {
      httpClient.close()
    }

    if(statusCode == 200 || statusCode == 201) {

      implicit val formats = Serialization.formats(
        ShortTypeHints(
          List()
        )
      )

      return read[PowerBIDatasetDetails](responseContent)
    }

    throw PowerBIClientException(statusCode, responseContent, exceptionMessage)
  }
}