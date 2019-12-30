/*
 * Anonymous API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.binwell.dal.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * GranitDto
 */

public class GranitDto {
  @SerializedName("id")
  private Integer id = null;

  @SerializedName("blockNumber")
  private Integer blockNumber = null;

  @SerializedName("blockTypeId")
  private Integer blockTypeId = null;

  @SerializedName("objectId")
  private Integer objectId = null;

  @SerializedName("blockType")
  private BlockTypeDto blockType = null;

  public GranitDto id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public GranitDto blockNumber(Integer blockNumber) {
    this.blockNumber = blockNumber;
    return this;
  }

   /**
   * Get blockNumber
   * @return blockNumber
  **/

  public Integer getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(Integer blockNumber) {
    this.blockNumber = blockNumber;
  }

  public GranitDto blockTypeId(Integer blockTypeId) {
    this.blockTypeId = blockTypeId;
    return this;
  }

   /**
   * Get blockTypeId
   * @return blockTypeId
  **/

  public Integer getBlockTypeId() {
    return blockTypeId;
  }

  public void setBlockTypeId(Integer blockTypeId) {
    this.blockTypeId = blockTypeId;
  }

  public GranitDto objectId(Integer objectId) {
    this.objectId = objectId;
    return this;
  }

   /**
   * Get objectId
   * @return objectId
  **/

  public Integer getObjectId() {
    return objectId;
  }

  public void setObjectId(Integer objectId) {
    this.objectId = objectId;
  }

  public GranitDto blockType(BlockTypeDto blockType) {
    this.blockType = blockType;
    return this;
  }

   /**
   * Get blockType
   * @return blockType
  **/

  public BlockTypeDto getBlockType() {
    return blockType;
  }

  public void setBlockType(BlockTypeDto blockType) {
    this.blockType = blockType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GranitDto granitDto = (GranitDto) o;
    return Objects.equals(this.id, granitDto.id) &&
        Objects.equals(this.blockNumber, granitDto.blockNumber) &&
        Objects.equals(this.blockTypeId, granitDto.blockTypeId) &&
        Objects.equals(this.objectId, granitDto.objectId) &&
        Objects.equals(this.blockType, granitDto.blockType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, blockNumber, blockTypeId, objectId, blockType);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GranitDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    blockNumber: ").append(toIndentedString(blockNumber)).append("\n");
    sb.append("    blockTypeId: ").append(toIndentedString(blockTypeId)).append("\n");
    sb.append("    objectId: ").append(toIndentedString(objectId)).append("\n");
    sb.append("    blockType: ").append(toIndentedString(blockType)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

