package com.simpletwodo.batch

/**
 * Batch to change to false display flag of user task.
 * User: mao
 * Date: 12/04/12
 * Time: 23:00
 * This batch works on every day (scheduled by heroku scheduler add-on).
 * Display flag of task that taskStaus is true is changed false.
 */

import com.simpletwodo.mongodbutil._

object ChangeTaskDisplay extends App {
  SimpleTwoDoDatabase.getAllUserData.foreach(userData => {
    val taskUpdatedUser = userData.updateUserTasks(
      userData.userTaskList.map(task =>
        if (task.taskStatus == true && task.displayFlag == true)
          SimpleTwoDoTask(
            task.tweetId,
            task.tweetStatus,
            task.taskStatus,
            false
          )
        else task
      )
    )

    try {
      SimpleTwoDoDatabase.updateUserData(taskUpdatedUser)
    } catch {
      case ex: Exception =>
        println("Task update failed: %str".format(ex.getMessage))
    }
  })
}
