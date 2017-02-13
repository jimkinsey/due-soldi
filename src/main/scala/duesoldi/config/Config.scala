package duesoldi.config

import scala.concurrent.duration.Duration

case class Config(blogStorePath: String, furnitureVersion: String, furniturePath: String, furnitureCacheDuration: Duration)
