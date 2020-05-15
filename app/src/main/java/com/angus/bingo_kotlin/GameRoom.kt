package com.angus.bingo_kotlin

class GameRoom(
    var id: String,
    var status: Int,
    var title: String,
    var init: Member?,
    var join: Member?
){
    constructor() : this("", 0, "wellcome", null, null)
    constructor(title : String, init: Member?) : this("", 0, title, init, null)
}