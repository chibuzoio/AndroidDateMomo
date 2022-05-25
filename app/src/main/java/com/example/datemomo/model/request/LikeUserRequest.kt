package com.example.datemomo.model.request

data class LikeUserRequest(var memberId: Int,
                           var liked: Boolean,
                           var likedUserId: Int)


