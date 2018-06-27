package duesoldi.metrics

import duesoldi.metrics.storage.AccessRecordStore.Access

package object rendering
{
  type MakeAccessCsv = (List[Access]) => String
}
