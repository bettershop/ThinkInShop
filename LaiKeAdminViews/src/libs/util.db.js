import low from 'lowdb';
import sessionStorage from 'lowdb/adapters/sessionStorage';

const adapter = new sessionStorage('admin');
const db = low(adapter);

db
  .defaults({
    sys: {},
    database: {}
  })
  .write();

export default db;
