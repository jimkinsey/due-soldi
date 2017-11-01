package duesoldi.dependencies

import duesoldi.config.Config

object Injection
{
  type Inject[DEP] = Config => DEP

  def inject[IN, OUT](fn: IN => OUT)(implicit in: Config => IN): Inject[OUT] = config => fn(in(config))

  def inject[IN1, IN2, OUT](fn: (IN1, IN2) => OUT)(implicit in1: Inject[IN1], in2: Inject[IN2]): Inject[OUT] = config => fn(in1(config), in2(config))

  def inject[IN1, IN2, IN3, OUT](fn: (IN1, IN2, IN3) => OUT)(implicit in1: Inject[IN1], in2: Inject[IN2], in3: Inject[IN3]): Inject[OUT] = config => fn(in1(config), in2(config), in3(config))

  def inject[IN1, IN2, IN3, IN4, OUT](fn: (IN1, IN2, IN3, IN4) => OUT)(implicit in1: Inject[IN1], in2: Inject[IN2], in3: Inject[IN3], in4: Inject[IN4]): Inject[OUT] = config => fn(in1(config), in2(config), in3(config), in4(config))

  def inject[IN1, IN2, IN3, IN4, IN5, OUT](fn: (IN1, IN2, IN3, IN4, IN5) => OUT)(implicit in1: Inject[IN1], in2: Inject[IN2], in3: Inject[IN3], in4: Inject[IN4], in5: Inject[IN5]): Inject[OUT] = (config: Config) => fn(in1(config), in2(config), in3(config), in4(config), in5(config))

  def inject[IN1, IN2, IN3, IN4, IN5, IN6, OUT](fn: (IN1, IN2, IN3, IN4, IN5, IN6) => OUT)(implicit in1: Inject[IN1], in2: Inject[IN2], in3: Inject[IN3], in4: Inject[IN4], in5: Inject[IN5], in6: Inject[IN6]): Inject[OUT] = (config: Config) => fn(in1(config), in2(config), in3(config), in4(config), in5(config), in6(config))
}
