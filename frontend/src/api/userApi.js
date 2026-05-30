import api from './axios';

export const getNotifications    = ()       => api.get('/api/notifications');
export const markNotificationRead = (id)    => api.put(`/api/notifications/${id}/read`);
export const deleteNotification   = (id)    => api.delete(`/api/notifications/${id}`);

export const getLists        = ()           => api.get('/api/lists');
export const createList      = (data)       => api.post('/api/lists', data);
export const updateList      = (id, data)   => api.put(`/api/lists/${id}`, data);
export const deleteList      = (id)         => api.delete(`/api/lists/${id}`);
export const addMovieToList  = (id, movieId) => api.post(`/api/lists/${id}/movies`, { movieId });

export const followUser   = (userId) => api.post(`/api/follow/${userId}`);
export const unfollowUser = (userId) => api.delete(`/api/follow/${userId}`);
export const getFollowers = ()       => api.get('/api/follow/followers');
export const getFollowing = ()       => api.get('/api/follow/following');
