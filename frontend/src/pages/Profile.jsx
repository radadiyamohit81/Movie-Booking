import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getNotifications, markNotificationRead, deleteNotification } from '../api/userApi';
import { getLists, createList, deleteList } from '../api/userApi';

export default function Profile() {
  const { user, logout } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [lists,         setLists]         = useState([]);
  const [newListName,   setNewListName]   = useState('');
  const [activeTab,     setActiveTab]     = useState('notifications');

  useEffect(() => {
    getNotifications().then(({ data }) => setNotifications(data)).catch(() => {});
    getLists().then(({ data }) => setLists(data)).catch(() => {});
  }, []);

  const handleMarkRead = async (id) => {
    await markNotificationRead(id);
    setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, read: true } : n));
  };

  const handleDeleteNotif = async (id) => {
    await deleteNotification(id);
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const handleCreateList = async (e) => {
    e.preventDefault();
    if (!newListName.trim()) return;
    const { data } = await createList({ name: newListName.trim(), isPublic: false });
    setLists((prev) => [data, ...prev]);
    setNewListName('');
  };

  const handleDeleteList = async (id) => {
    await deleteList(id);
    setLists((prev) => prev.filter((l) => l.id !== id));
  };

  const tabs = ['notifications', 'lists'];

  return (
    <div className="container" style={{ paddingTop: '40px', paddingBottom: '60px' }}>
      {/* Profile header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '40px' }}>
        <div style={{
          width: '80px', height: '80px', borderRadius: '50%', background: 'var(--accent)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '32px', fontWeight: 800, color: '#000',
        }}>
          {user?.username?.[0]?.toUpperCase()}
        </div>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: 800 }}>{user?.username}</h1>
          <span style={{ fontSize: '13px', padding: '3px 10px', background: user?.role === 'ROLE_ADMIN' ? 'var(--accent)' : 'var(--bg-card)', color: user?.role === 'ROLE_ADMIN' ? '#000' : 'var(--text-secondary)', borderRadius: '20px', fontWeight: 600 }}>
            {user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Member'}
          </span>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={logout} style={{ marginLeft: 'auto' }}>Logout</button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '4px', marginBottom: '24px', background: 'var(--bg-card)', padding: '4px', borderRadius: 'var(--radius)', width: 'fit-content' }}>
        {tabs.map((t) => (
          <button key={t} onClick={() => setActiveTab(t)} style={{
            padding: '8px 18px', borderRadius: '6px', border: 'none', fontWeight: 600, fontSize: '14px', cursor: 'pointer', textTransform: 'capitalize',
            background: activeTab === t ? 'var(--accent)' : 'transparent',
            color: activeTab === t ? '#000' : 'var(--text-secondary)',
          }}>{t}</button>
        ))}
      </div>

      {/* Notifications tab */}
      {activeTab === 'notifications' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {notifications.length === 0 && <div className="empty-state"><h3>No notifications</h3></div>}
          {notifications.map((n) => (
            <div key={n.id} style={{
              background: n.read ? 'var(--bg-card)' : 'rgba(245,197,24,0.08)',
              border: `1px solid ${n.read ? 'var(--border)' : 'rgba(245,197,24,0.3)'}`,
              borderRadius: 'var(--radius)', padding: '14px 16px',
              display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px',
            }}>
              <div>
                <span style={{ fontSize: '12px', color: 'var(--accent)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px' }}>{n.type}</span>
                <p style={{ fontSize: '14px', color: 'var(--text-primary)', marginTop: '2px' }}>{n.message}</p>
              </div>
              <div style={{ display: 'flex', gap: '8px', flexShrink: 0 }}>
                {!n.read && <button className="btn btn-secondary btn-sm" onClick={() => handleMarkRead(n.id)}>Mark read</button>}
                <button className="btn btn-danger btn-sm" onClick={() => handleDeleteNotif(n.id)}>🗑</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Lists tab */}
      {activeTab === 'lists' && (
        <div>
          <form onSubmit={handleCreateList} style={{ display: 'flex', gap: '10px', marginBottom: '24px' }}>
            <input className="form-control" placeholder="New list name…" value={newListName}
              onChange={(e) => setNewListName(e.target.value)} style={{ maxWidth: '300px' }} />
            <button type="submit" className="btn btn-primary">Create List</button>
          </form>

          {lists.length === 0 && <div className="empty-state"><h3>No custom lists</h3><p>Create a list to curate your favourite movies.</p></div>}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {lists.map((l) => (
              <div key={l.id} style={{
                background: 'var(--bg-card)', border: '1px solid var(--border)',
                borderRadius: 'var(--radius)', padding: '14px 16px',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              }}>
                <div>
                  <p style={{ fontWeight: 600, fontSize: '15px' }}>{l.name}</p>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                    {l.movieIds?.length || 0} movies · {l.isPublic ? '🌐 Public' : '🔒 Private'}
                  </p>
                </div>
                <button className="btn btn-danger btn-sm" onClick={() => handleDeleteList(l.id)}>Delete</button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
