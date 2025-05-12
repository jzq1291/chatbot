import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8082/ai/knowledge',
  timeout: 300000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export interface KnowledgeBase {
    id?: number;
    title: string;
    content: string;
    category: string;
}

export const knowledgeApi = {
    // 添加知识
    async addKnowledge(knowledge: KnowledgeBase) {
        try {
            const response = await api.post<KnowledgeBase>('', knowledge);
            return response;
        } catch (error) {
            console.error('添加知识失败:', error);
            throw error;
        }
    },

    // 搜索知识
    async searchKnowledge(keyword: string) {
        try {
            console.log('搜索知识，关键词:', keyword);
            const response = await api.get<KnowledgeBase[]>('/search', {
                params: { keyword }
            });
            console.log('搜索结果:', response.data);
            return response;
        } catch (error) {
            console.error('搜索知识失败:', error);
            throw error;
        }
    },

    // 按分类获取知识
    async getByCategory(category: string) {
        try {
            const response = await api.get<KnowledgeBase[]>(`/category/${category}`);
            return response;
        } catch (error) {
            console.error('获取分类知识失败:', error);
            throw error;
        }
    },

    // 删除知识
    async deleteKnowledge(id: number) {
        try {
            const response = await api.delete(`/${id}`);
            return response;
        } catch (error) {
            console.error('删除知识失败:', error);
            throw error;
        }
    },

    // 更新知识
    async updateKnowledge(knowledge: KnowledgeBase) {
        try {
            if (!knowledge.id) throw new Error('缺少知识ID');
            const response = await api.put<KnowledgeBase>(`/${knowledge.id}`, knowledge);
            return response;
        } catch (error) {
            console.error('更新知识失败:', error);
            throw error;
        }
    }
}; 