package com.chibuzo.datemomo.model

import kotlinx.serialization.Serializable

@Serializable
data class PictureCollectionModel(var pictureLayoutType: String,
                                  var galleryPictureModels: ArrayList<GalleryPictureModel>)


