package com.angus.bingo_kotlin

data class Member(var uid : String,
                  var displayName : String,
                  var nickName : String?,
                  var avatarId : Int){
    constructor() : this("", "", null, 0)
}